

function getConfig() {

    let cfg = {
        buildTime: process.env.REACT_APP_BUILD_TIME,
        versionHash: process.env.REACT_APP_VERSION_HASH,
        baseUrl: document.location.protocol + "//" + document.location.host,
        wsUrl: "ws//" + document.location.host + "/ws",
        logState: false
    }

    let development = !process.env.NODE_ENV || process.env.NODE_ENV === 'development';
    if (development) {
        cfg["logState"] = true
        cfg["baseUrl"] = "http://localhost:2739"
        cfg["wsUrl"] = "ws://localhost:2739/ws"
    }
    return cfg
}



export const config = getConfig()
