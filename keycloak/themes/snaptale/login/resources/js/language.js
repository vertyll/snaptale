// The language the reader last chose, in the app or on Keycloak's pages, is kept in the cookie `ui_locale_<realm>`,
// which the app reads whenever a page loads and writes whenever its language changes; a choice made here goes in it, so
// the app shows it however the reader returns. Keycloak itself keeps the choice on the account, for its own pages.
(() => {
  document.addEventListener("click", (event) => {
    const choice = event.target.closest("a[lang][href*='kc_locale']");
    if (choice) {
      handOff(choice.lang);
    }
  });

  // The cookie lives under the domain Keycloak shares with the app, the parent of this host; on localhost and an IP
  // address every port already shares cookies.
  function handOff(language) {
    const realm = window.location.pathname.match(/\/realms\/([^/]+)\//)?.[1];
    if (!realm || !/^[a-z]{2,3}(-[A-Za-z0-9]+)*$/.test(language)) {
      return;
    }
    const labels = window.location.hostname.split(".");
    const isAddress = labels.every((label) => /^\d+$/.test(label));
    const domain = labels.length > 2 && !isAddress ? `; Domain=${labels.slice(1).join(".")}` : "";
    const secure = window.location.protocol === "https:" ? "; Secure" : "";
    document.cookie = `ui_locale_${realm}=${language}; Path=/; Max-Age=31536000; SameSite=Lax${domain}${secure}`;
  }
})();
