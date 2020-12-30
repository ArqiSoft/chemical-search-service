module.exports = {
    css: {
        loaderOptions: {
            sass: {
                additionalData: `@import "@/assets/styles/styles.scss";`,
            },
        },
    },
    chainWebpack: config => {
        config
            .plugin('html')
            .tap(args => {
                args[0].title = "Chemical Search";
                return args;
            })
    }
};