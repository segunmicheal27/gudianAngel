function locateReq(){
    req.sendBack();
    window.location.href = "dashboard.html";
 }



function changeElementContent(id, newContent){
     document.getElementById(id).innerHTML = newContent;
}

function hideElement(id,prop,show){

var link = document.getElementById(''+id+'');
link.style.display = ''+prop+''; //or
link.style.visibility = ''+show+'';

}


function changeInputElementContent(id, newContent){
        $("#"+id+"").val(newContent);
}


function DeviceLoginType() {
   return dev.getLoginType();
}

function getType() {
   return getEmgSessType.getEmgType();
}


//function getEmergencyState() {
//   return req.getEmgState;
//}

function setEmgType(type,id) {
   return reqType.reqEmg(type,id);
}



function DeviceType() {
   return dev.getDeviceType();
 }

function DeviceVersion() {
   return dev.getDeviceVersion();
 }

function DeviceSerialNumber() {
   return dev.getDeviceSerialNumber();
 }

function DeviceMANUFACTURER() {
   return dev.getDeviceMANUFACTURER();
 }


function submitAjaxForm(){
// Assign handlers immediately after making the request,
// and remember the jqxhr object for this request

//alert(DeviceLoginType());
//alert(DeviceType());
//alert(DeviceVersion());
//alert(DeviceSerialNumber());

 $('.sweet-loader').show();

if (DeviceLoginType() == "old"){

//alert ("old device");

    var jqxhr = $.post( "http://guardianangels.com.ng/application/public/api/auth", {
    login_data_web_view: 'login_data_web_view',
    dev_type: 'old',
    username: $("#username").val(),
    password: $("#password").val()
     })
      .done(function(data) {
    //    alert( "second success: "+JSON.stringify(data.message) );
        login_result.getJSONTData(JSON.stringify(data));
        console.log(data);
      })
      .fail(function(err) {
      $('.sweet-loader').hide();
        console.log(err);
      })
      .always(function(data) {
            if (data.message !== "Logined in!"){
            $('.sweet-loader').hide();
                $('#error').text("Login failed! incorrect login credentials");
                    $('#error').addClass("success").css("color", "red").fadeIn();
                    $('#error').addClass("success").css("font-size", "22px");
            }else{
                window.location.href = "dashboard.html";
            }
      });

}else{

//    alert ("new device");

    var jqxhr = $.post( "http://guardianangels.com.ng/application/public/api/auth", {
    login_data_web_view: 'login_data_web_view',
    dev_type: "new_device",
    device_id: DeviceSerialNumber(),
    devices_type: DeviceType(),
    version: "v1.0",
    device_manufacture: DeviceMANUFACTURER(),
    username: $("#username").val(),
    password: $("#password").val()
    })
          .done(function(data) {
            login_result.getJSONTData(JSON.stringify(data));
            console.log(data);
          })
          .fail(function(err) {
          $('.sweet-loader').hide();
            console.log(err);
          })
          .always(function(data) {
                if (data.message !== "Logined in!"){
                $('.sweet-loader').hide();
                    $('#error').text(data.message);
                        $('#error').addClass("success").css("color", "red").fadeIn();
                        $('#error').addClass("success").css("font-size", "22px");
                }else{
                    window.location.href = "dashboard.html";
                }
          });

}

// Perform other work here ...
// Set another completion function for the request above
 }

function submitAjaxRegForm(){


         $.ajaxSetup({
           beforeSend: function() {

           $('.sweet-loader').show();
           $(".sweet-loader").css("display", "block");

          }
        });

    var jqxhr = $.post("http://guardianangels.com.ng/application/public/api/auth", {
    reg_web_view_submit_data: 'reg_web_view_submit_data',
   names: $("#name").val(),
       phone: $("#phone").val(),
       email: $("#email").val(),
       username: $("#user").val(),
       password: $("#pass").val(),
        device_id: DeviceSerialNumber(),
        devices_type: DeviceType(),
        version: "v1.0",
        device_manufacture: DeviceMANUFACTURER()
    })
          .done(function(data) {
                            reg_result.getRegData(JSON.stringify(data));
                      console.log(JSON.stringify(data));
                    })
          .fail(function(err) {
          $('.sweet-loader').hide();

          $('#error').text(err);
          $('#error').addClass("success").css("color", "red").fadeIn();
          $('#error').addClass("success").css("font-size", "22px");

            console.log(err);

          }).always(function(data) {

                if (data.message !== "success!"){
                $('.sweet-loader').hide();
                    $('#error').text(data.message);
                        $('#error').addClass("success").css("color", "red").fadeIn();
                        $('#error').addClass("success").css("font-size", "22px");
                }else{
                    window.location.href = "dashboard.html";
                }
          });


 }

function __el(title, body, author){
    $str = '<div class="form-mini-divider"></div>';
    $str += '<div class="news-list-item no-image">';
    $str += '<div class="list-content">';
    $str += '<span class="list-category orange">Security News</span>';
    $str += '<h2 class="list-title"><a href="#">'+ title +'</a></h2>';
    $str += '<p>'+ body +'</p>';
    $str += '<a href="#" class="list-author">'+ author +'</a>';
    $str += '</div>';
    $str += '</div>';

    return $str;
}

function getNews(){
    $.ajax({
        url: "http://guardianangels.com.ng/application/public/api/news",
         beforeSend: function() {
            $('.sweet-loader').show();
            },
        success: function(data){
            $('.sweet-loader').hide();

            for(var i in data){
                $('#news-box').append(__el(data[i].title, data[i].body, data[i].author  ) );
            }
            console.log(data);

        },
        error: function(err, res){
            console.log(err, res);
        }
    });
 }

function submitAjaxEmgRegForm(id){


    if (getType() === "empty" ){

        $.ajaxSetup({
           beforeSend: function() {
           $('.sweet-loader').show();
          }
        });



        var jqxhr = $.post("http://guardianangels.com.ng/application/public/api/auth", {

            emg_reg_submit_data: 'emg_reg_submit_data',
                user_id: $("#user_id").val(),
                device_id: $("#device_id").val(),

                name: $("#name").val(),
                phone: $("#phone").val(),
                email: $("#email").val(),
                address: $("#address").val(),
                emg_type: $("#"+id+"").attr('data-req')

            }).done(function(data) {
                   $('.sweet-loader').hide();
                    console.log(data);
                  })
                  .fail(function(err) {
                  $('.sweet-loader').hide();
                    console.log(err);
                  }).always(function(data) {

                        if (data.message !== "success!"){

                        $('.sweet-loader').hide();

                        $('#error').text("Request failed! try again "+data.message);
                        $('#error').addClass("success").css("color", "red").fadeIn();
                        $('#error').addClass("success").css("font-size", "22px");

                        }else{

                        $('#error').text("request received, we will get back to you shortly");
                        $('#error').addClass("success").css("color", "green").fadeIn();
                        $('#error').addClass("success").css("font-size", "22px");

                         req.setEmgReq();
                         setEmgType($("#"+id+"").attr("data-req"),data.id);
                         window.location.href = "emergency.html";

                        }
                  });

        }else{
        setEmgType($("#"+id+"").attr("data-req"),0);
        window.location.href = "emergency.html";
        }
 }


function submitAjaxVetRegForm(){


if ($("#vet_type").val() == "") {

    $('.sweet-loader').hide();

    $('#error').text("Emergency type required");
    $('#error').addClass("success").css("color", "red").fadeIn();
    $('#error').addClass("success").css("font-size", "22px");


    }else if ($("#address").val() == ""){

    $('.sweet-loader').hide();
        $('#error').text("Address required!");
        $('#error').addClass("success").css("color", "red").fadeIn();
        $('#error').addClass("success").css("font-size", "22px");


    }else{

        $.ajaxSetup({
           beforeSend: function() {
           $('.sweet-loader').show();
          }
        });

        var jqxhr = $.post("http://guardianangels.com.ng/application/public/api/auth", {
            vetted_reg_submit_data: 'vetted_reg_submit_data',
                name: $("#name").val(),
                phone: $("#phone").val(),
                email: $("#email").val(),
                address: $("#address").val(),
                add_info: $("#add_info").val(),
                vet_type: $("#vet_type").val()
            })
            .done(function(data) {
                   $('.sweet-loader').hide();
                    console.log(data);
                  })
                  .fail(function(err) {
                  $('.sweet-loader').hide();
                    console.log(err);
                  })
                  .always(function(data) {

                        if (data.message !== "success!"){

                        $('.sweet-loader').hide();
                        $('#error').text("Request failed! try again"+data.message);
                        $('#error').addClass("success").css("color", "red").fadeIn();
                        $('#error').addClass("success").css("font-size", "22px");

                        }else{
                        $('#error').text("request received, we will get back to you shortly");
                        $('#error').addClass("success").css("color", "green").fadeIn();
                        $('#error').addClass("success").css("font-size", "22px");
                        }
                  });
    }
 }

function submitAjaxEventRegForm(){


if ($("#event_type").val() == "") {

    $('.sweet-loader').hide();

    $('#error').text("Emergency type required");
    $('#error').addClass("success").css("color", "red").fadeIn();
    $('#error').addClass("success").css("font-size", "22px");


    }else if ($("#address").val() == ""){

    $('.sweet-loader').hide();
        $('#error').text("Address required!");
        $('#error').addClass("success").css("color", "red").fadeIn();
        $('#error').addClass("success").css("font-size", "22px");


    }else{

        $.ajaxSetup({
           beforeSend: function() {
           $('.sweet-loader').show();
          }
        });

        var jqxhr = $.post("http://guardianangels.com.ng/application/public/api/auth", {
            event_reg_submit_data: 'event_reg_submit_data',
                name: $("#name").val(),
                phone: $("#phone").val(),
                email: $("#email").val(),
                address: $("#address").val(),
                add_info: $("#add_info").val(),
                event_type: $("#event_type").val()
            })
            .done(function(data) {
                   $('.sweet-loader').hide();
                    console.log(data);
                  })
                  .fail(function(err) {
                  $('.sweet-loader').hide();
                    console.log(err);
                  })
                  .always(function(data) {

                        if (data.message !== "success!"){

                        $('.sweet-loader').hide();
                        $('#error').text("Request failed! try again"+data.message);
                        $('#error').addClass("success").css("color", "red").fadeIn();
                        $('#error').addClass("success").css("font-size", "22px");

                        }else{
                        $('#error').text("request received, we will get back to you shortly");
                        $('#error').addClass("success").css("color", "green").fadeIn();
                        $('#error').addClass("success").css("font-size", "22px");
                        }
                  });
    }
 }

function submitAjaxBodyRegForm(){


if ($("#body_type").val() == "") {

    $('.sweet-loader').hide();

    $('#error').text("Emergency type required");
    $('#error').addClass("success").css("color", "red").fadeIn();
    $('#error').addClass("success").css("font-size", "22px");


    }else if ($("#address").val() == ""){

    $('.sweet-loader').hide();
        $('#error').text("Address required!");
        $('#error').addClass("success").css("color", "red").fadeIn();
        $('#error').addClass("success").css("font-size", "22px");


    }else{

        $.ajaxSetup({
           beforeSend: function() {
           $('.sweet-loader').show();
          }
        });

        var jqxhr = $.post("http://guardianangels.com.ng/application/public/api/auth", {
            body_reg_submit_data: 'body_reg_submit_data',
                name: $("#name").val(),
                phone: $("#phone").val(),
                email: $("#email").val(),
                address: $("#address").val(),
                add_info: $("#add_info").val(),
                body_type: $("#body_type").val()
            })
            .done(function(data) {
                   $('.sweet-loader').hide();
                    console.log(data);
                  })
                  .fail(function(err) {
                  $('.sweet-loader').hide();
                    console.log(err);
                  })
                  .always(function(data) {

                        if (data.message !== "success!"){

                        $('.sweet-loader').hide();
                        $('#error').text("Request failed! try again"+data.message);
                        $('#error').addClass("success").css("color", "red").fadeIn();
                        $('#error').addClass("success").css("font-size", "22px");

                        }else{
                        $('#error').text("request received, we will get back to you shortly");
                        $('#error').addClass("success").css("color", "green").fadeIn();
                        $('#error').addClass("success").css("font-size", "22px");
                        }
                  });
    }
 }

function submitAjaxNormalRegForm(){


if ($("#normal_type").val() == "") {

    $('.sweet-loader').hide();


    $('#error').text("Emergency type required");
    $('#error').addClass("success").css("color", "red").fadeIn();
    $('#error').addClass("success").css("font-size", "22px");



    }else if ($("#address").val() == ""){

    $('.sweet-loader').hide();


        $('#error').text("Address required!");
        $('#error').addClass("success").css("color", "red").fadeIn();
        $('#error').addClass("success").css("font-size", "22px");


    }else{
            $.ajaxSetup({
              beforeSend: function (jqXHR, settings) {
                $('.sweet-loader').show();
              }
            });

        var jqxhr = $.post("http://guardianangels.com.ng/application/public/api/auth", {
            normal_reg_submit_data: 'normal_reg_submit_data',
                name: $("#name").val(),
                phone: $("#phone").val(),
                email: $("#email").val(),
                address: $("#address").val(),
                add_info: $("#add_info").val(),
                normal_type: $("#normal_type").val()
            })
            .done(function(data) {
                   $('.sweet-loader').hide();
                    console.log(data);
                  })
                  .fail(function(err) {
                  $('.sweet-loader').hide();
                    console.log(err);
                  })
                  .always(function(data) {
                    $('.sweet-loader').hide();
                        if (data.message !== "success!"){

                        $('#error').text("Request failed! try again"+data.message);
                        $('#error').addClass("success").css("color", "red").fadeIn();
                        $('#error').addClass("success").css("font-size", "22px");

                        }else{
                        $('#error').text("request received, we will get back to you shortly");
                        $('#error').addClass("success").css("color", "green").fadeIn();
                        $('#error').addClass("success").css("font-size", "22px");
                        }
                  });
    }
 }