/** 定义控制器层 */
app.controller('userController', function($scope, $timeout, baseService){

    $scope.user = {};

    $scope.save = function () {
        if ($scope.smsCode) {
            if ($scope.user.password == $scope.password) {
                baseService.sendPost("/user/save?smsCode=" + $scope.smsCode, $scope.user).then(function (response) {
                    if (response.data) {
                        alert("注册成功");
                        $scope.user = {};
                        $scope.password = {};
                        $scope.smsCode = "";
                    } else {
                        alert("验证码错误")
                    }
                })
            } else {
                alert("两次输入的密码不一致");
            }
        } else {
            alert("请输入验证码")
        }
    };

    $scope.sendMsg = function () {
        if ($scope.user.phone && /^1[3|4|5|7|8|9]\d{9}$/.test($scope.user.phone)) {
            baseService.sendGet("/user/sendCode?phone=" + $scope.user.phone).then(function (response) {
                if (response.data) {
                    $scope.flag = true;
                    $scope.downCount(90);

                } else {
                    alert("发送验证码失败");
                }
            })
        } else {
            alert("手机号输入有误");
        }
    };

    $scope.tipMsg = "获取短信验证码";
    $scope.flag = false;

    $scope.downCount = function (second) {
        if (second > 0) {
            second--;
            $scope.tipMsg = second + "秒后重新获取验证码";

            //开启定时器
            $timeout(function () {
                $scope.downCount(second);
            })
        } else {
            $scope.tipMsg = "获取短信验证码";
            $scope.flag = false;
        }
    }
});