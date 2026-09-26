package com.vertyll.snaptale.post;

import java.util.List;

record FeedPage(List<PostCard> posts, boolean hasMore) {
}
