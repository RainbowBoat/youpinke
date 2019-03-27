app.controller('indexController', function($scope,$controller,$location,baseService){
    $scope.showLoginName = function () {
        $scope.redirectUrl = window.encodeURIComponent(location.href);
        baseService.sendGet("/user/showLoginName").then(function (response) {
            $scope.loginName = response.data.loginName;
        })
    };
    //图片上传
    $scope.uploadPicture= function () {
        baseService.uploadFile().then(function (value) {
            if (value.status == 200) {
                $scope.user.headPic = value.data.url;
            }
        });
    };
    /** 定义用户对象 */
    $scope.user = {nickName:"",sex:"",birthday:"",headPic:"",address:{}};
    /** 添加或修改 */
    $scope.saveOrUpdate = function(){
        var url = "saveUser";
        /** 发送post请求 */
        baseService.sendPost("/user/" + url, $scope.user)
            .then(function(response){
                if (response.data){
                    alert("加载成功")
                }else{
                    alert("操作失败！");
                }
            });
    };
    /**
     * 地区分类选择
     */
    $scope.findProvincesByParentId = function (name) {
        baseService.sendGet("/user/findAll").then(function (value) {
                $scope[name] = value.data;

        });
    };
    $scope.$watch('user.address.provinceId', function (newVal, oldVal) {
        if (newVal) {
            var num=newVal-1;

            $scope.findProvincesByTwo(JSON.stringify($scope.ProvincesList1[num].provinceId));
        } else {
            $scope.Provinces1 = [];
        }
    });
    $scope.findProvincesByTwo=function(provinceId){
        baseService.sendGet("/user/findProvincesByTwo?provinceId="+JSON.parse(provinceId)).then(function(response){
                $scope.ProvincesList2=response.data
        })
    }
    $scope.$watch('user.address.cityId', function (newVal, oldVal) {
        if (newVal) {
            var num=newVal-1;
            $scope.findProvincesByThree(JSON.stringify($scope.ProvincesList2[num].cityId));
        } else {
            $scope.Provinces2 = [];
        }
    });
    $scope.findProvincesByThree=function(cityId){
        baseService.sendGet("/user/findProvincesByThree?cityId="+JSON.parse(cityId)).then(function(response){
            $scope.ProvincesList3=response.data
        })
    }



    //用户密码设置
    $scope.submit =function(){
        if($scope.password!=$scope.user.password){
            alert("密码不一致");
            $scope.password="";
            $scope.user.password="";
            return;
        }
        baseService.sendPost("/user/submit",$scope.user).then(function(response){
            if(response.data){
                alert("提交成功")
                $scope.user={};
                $scope.password="";
            }else{
                alert("提交失败")
            }
        })
    }
    /** 定义发送短信验证码方法 */
    $scope.sendCode = function () {
        if ($scope.phone){
            baseService.sendGet("/user/sendCode?phone="+ $scope.phone).then(function(response){
                alert(response.data)
                    alert(response.data ? "发送成功！" : "发送失败！");

                });
        }else {
            alert("请输入手机号码！");
        }
    };
  //显示手机号
    $scope.findNumber =function(){
        baseService.sendGet("/user/findNumber").then(function(response){
            $scope.phone = response.data;
        })
    };
    //绑定手机的下一步
    $scope.Click=function(){
        if($scope.YZ==null){
            alert("验证码不能为空")
        }else{
            location.href="http://user.pinyougou.com/home-setting-address-phone.html";
        }
    }
});