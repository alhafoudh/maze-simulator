var init = function () {
    console.log("Initialize script");
};

function sensor(sensorIndex) {
    return state.sensors[sensorIndex];
}

function setMotor(leftMotor, rightMotor) {
    state.leftMotor = leftMotor;
    state.rightMotor = rightMotor;
}

var update = function () {
    var fl = sensor(0);
    var fr = sensor(1);
    var ll = sensor(2);
    var rr = sensor(3);
    var dl = sensor(4);
    var dr = sensor(5);

    console.log(
        "SENSOR: "
        + ll + " "
        + dl + " "
        + fl + " "
        + " | "
        + fr + " "
        + dr + " "
        + rr + " "
    );

    setMotor(1000, 1000);
};
