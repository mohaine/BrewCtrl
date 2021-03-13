

import process from "process";


function getConfig() {
    let development = !process.env.NODE_ENV || process.env.NODE_ENV === 'development';

    if (development) {
        return {
            logState: true,
            baseUrl: "http://localhost:2739"
        }
    }
    return {
        logState: false,
        baseUrl: document.location.protocol + "//" + document.location.host
    }
}



export const config = getConfig()
