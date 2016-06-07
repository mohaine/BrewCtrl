
import {generateHexId} from '../util/id'

class RequestStatus {
    constructor() {
        this.id = generateHexId(16);
        this.status = "Active";
        this.active = true;
    }

    copy() {
        return Object.assign({}, this);
    }

    success() {
        return Object.assign({}, this, {
            status: "Success",
            active: false
        });
    }

    error(message) {
        return Object.assign({}, this, {
            status: "Errored",
            message,
            active: false
        });
    }



}

export default RequestStatus;
