#!/bin/sh
set -e

export PATH="/usr/local/bin:/usr/bin:/bin:$PATH"

echo "Starting Laravel application setup..."

# Check required environment variables
if [ -z "$DB_HOST" ] || [ -z "$DB_PORT" ]; then
    echo "Error: DB_HOST or DB_PORT is not set."
    exit 1
fi

# Wait for database
echo "Waiting for database at $DB_HOST:$DB_PORT..."
TIMEOUT=60
ELAPSED=0

while ! /usr/bin/nc -z "$DB_HOST" "$DB_PORT" 2>/dev/null; do
    if [ $ELAPSED -ge $TIMEOUT ]; then
        echo "Error: Database connection timeout after ${TIMEOUT}s"
        exit 1
    fi
    echo "Database is unavailable - sleeping (${ELAPSED}/${TIMEOUT}s)"
    /bin/sleep 2
    ELAPSED=$((ELAPSED + 2))
done

echo "Database is up - running migrations"
cd /var/www/html

# Recreate the storage skeleton (mounted volume starts empty!)
echo "Preparing storage structure..."
mkdir -p storage/framework/views \
         storage/framework/cache/data \
         storage/framework/sessions \
         storage/logs \
         storage/app/public \
         bootstrap/cache

# Set proper permissions for mounted storage volume
echo "Setting storage permissions..."
chown -R www-data:www-data /var/www/html/storage /var/www/html/bootstrap/cache
chmod -R 775 /var/www/html/storage /var/www/html/bootstrap/cache

# Drop any stale cached config (it may have view.compiled frozen as false)
php artisan config:clear
php artisan cache:clear 2>/dev/null || true

# Run migrations
php artisan migrate --force

# Cache config/routes/views
php artisan config:cache
php artisan route:cache
php artisan view:cache

# Create/recreate storage link
echo "Setting up storage symlink..."
rm -rf public/storage
php artisan storage:link

echo "Application setup completed successfully"
echo "Starting services..."

exec "$@"
