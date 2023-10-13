const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const params = new URLSearchParams();
    params.append('username', 'casuser');
    params.append('password', 'Mellon');
    params.append('passcode', '352410');
    params.append('duration', 'PT15S');

    await cas.doPost("https://localhost:8443/cas/actuator/awsSts",
        params, {
            'Content-Type': "application/x-www-form-urlencoded"
        }, res => {
            cas.log(res.data);
            throw 'Operation must fail to fetch credentials without mfa';
        }, error => {
            assert(error.response.status === 401);
            assert(error.response.data.toString().includes("Authentication failed"));
        });

})();
