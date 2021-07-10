// @ts-check
import proxy from 'http2-proxy';

const scalaVersion = "3.0.0"
const developmentMode = process.env.NODE_ENV === 'development'

/** @type {import('snowpack').SnowpackUserConfig } */
export default {
  mount: {
    'frontend/src/main/static/': '/',
    [`frontend/target/scala-${scalaVersion}/frontend-${developmentMode ? "fastopt" : "opt"}`]: '/scripts'
  },
  routes: [
    {
      src: '/api/.*',
      dest: (req, res) => proxy.web(req, res, {
        hostname: 'localhost',
        port: 8090,
      })
    },
    {
      match: 'routes',
      src: '.*',
      dest: '/index.html',
    },
  ],
  plugins: [
    '@snowpack/plugin-sass',
  ]
}