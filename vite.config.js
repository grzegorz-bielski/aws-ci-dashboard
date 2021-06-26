// @ts-check
import { defineConfig } from "vite"
import { resolve } from "path"
import { minifyHtml, injectHtml } from "vite-plugin-html"

defineConfig({})

const scalaVersion = "3.0.0"

export default ({ command, mode }) => {
  const mainJS = `./target/scala-${scalaVersion}/frontend-${
    mode === "production" ? "opt" : "fastopt"
  }/main.js`
  const script = `<script defer src="${mainJS}"></script>`

  return defineConfig({
    root: './frontend',
    server: {
      proxy: {
        "/api": {
          target: "http://localhost:8090",
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ""),
        },
      },
    },
    // publicDir: "./src/main/resources/public",
    plugins: [
      ...(mode === "production" ? [minifyHtml()] : []),
      injectHtml({
        injectData: {
          script,
        },
      }),
    ],
    // resolve: {
    //   alias: {
    //     stylesheets: resolve(
    //       __dirname,
    //       "./frontend/src/main/static/stylesheets"
    //     ),
    //   },
    // },
  })
}
