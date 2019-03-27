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
    }

    //回显用户数据 (zhang)
    $scope.showData = function () {
      baseService.sendGet("/seller/showData").then(function (response) {
          if(response.data){
              $scope.seller = response.data;
          }else{
              alert("操作失败!");
          }
      });
    };

    //修改商家数据
    $scope.update = function () {
      baseService.sendPost("/seller/update", $scope.seller)
          .then(function (response) {
              if(response.data){
                  $scope.reload();
              }else{
                  alert("操作失败!");
              }
          });
    };
});