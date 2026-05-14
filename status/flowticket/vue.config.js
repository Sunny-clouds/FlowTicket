const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    client: {
      overlay: {
        warnings: false,
        errors: true,
        runtimeErrors: (error) => ![
          'ResizeObserver loop completed with undelivered notifications.',
          'ResizeObserver loop limit exceeded'
        ].includes(error.message)
      }
    }
  }
})
