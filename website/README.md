# Website

The documentation is [built with Starlight](https://starlight.astro.build).

Use Node.js 22.23.4 or later for the website build. Release publishing runs only in the upstream `lysine-dev/retrofit` repository.

Common commands:

* `npm install` – install deps (must do first or if `package.json` changes)
* `npm run dev` – local, hot-reload server
* `npm run build` – produce distribution in `dist/`
  * `npm run preview` – local server of the distribution folder
  * `npm run build -- --mode release` – remove "snapshot" banner
