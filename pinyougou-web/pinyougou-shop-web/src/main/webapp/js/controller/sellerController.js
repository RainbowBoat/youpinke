/** 定义控制器层 */
app.controller('sellerController', function($scope, $controller, baseService){

    /** 指定继承baseController */
    $controller('baseController',{$scope:$scope});

    $scope.saveOrUpdate = function () {
        var url = "/save";
        if ($scope.id) {
            url = "/update"
        }
        baseService.sendPost("/seller" + url, $scope.seller).then(function (value) {
            if (value.data) {
                location.href = "/shoplogin.html";
            } else {
                alert("操作失败");
            }
        })
    };

    $scope.seller = {};

    $scope.savePassword=function () {

        if (!($scope.seller.password&&$scope.seller.newPassword&&$scope.newPassword)){
            alert("不能留空!");
            return;

        }else if ($scope.seller.newPassword != $scope.newPassword){
           alert("新密码不一致");
           return;

       }else if ($scope.seller.newPassword == $scope.seller.password){
            alert("新旧密码不能一致");
            return;
        }
        baseService.sendPost("/seller/savePassword",$scope.seller)
            .then(function (response) {
                if (response.data){
                    alert("修改成功!");
                    $scope.seller={};
                    $scope.newPassword="";
                    parent.location.href="http://shop.pinyougou.com/admin/index.html";
                }else {
                    alert("原密码错误")
                }
            })

    };

    $scope.cleanPassword=function () {
        $scope.seller={};
        $scope.newPassword="";
    };

    $scope.cleanSeller=function () {
        $scope.seller={};
    };
});