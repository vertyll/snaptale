// A language chosen on Keycloak's pages goes back to the app in the cookie `ui_locale_<realm>`, as the sign-in returns
// to an address that carries nothing else; the app takes it over and removes it. Keycloak itself keeps the choice on the
// account, for its own pages.
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
    document.cookie = `ui_locale_${realm}=${language}; Path=/; Max-Age=86400; SameSite=Lax${domain}${secure}`;
  }
})();
