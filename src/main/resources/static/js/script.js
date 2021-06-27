console.log("This is Script file")

const toggleSidebar = () => {
	if($('.sidebar').is(":visible"))
	{
		//true : need to close
		$(".sidebar").css("display","none");
		$(".content").css("margin-left","0%");
	}
	else
	{
	//false : need to show
	$(".sidebar").css("display","block");
		$(".content").css("margin-left","20%");
}
};

const search=()=>{

let query =$("#search-input").val();
    if(query == ""){
      $(".search-result").hide();
    }else{
    console.log(query);
    //sending request
    let url=`http://localhost:8080/search/${query}`;
    fetch(url).then((response)=>{
         return response.json();
    }).then((data)=>{
        //console.log(data);

        let text=`<div class ='list-group'>`
        data.forEach((contact)=>{
             text+=` <a href='/user/${contact.cId}/contact' class ='list-group-item list-group-item-action'> ${contact.name} </a>`
        });
        text+= `</div>`;
        $(".search-result").html(text);
          $(".search-result").show();
        });
    }
};

//first request to server to create order
const paymentStart=()=>{
console.log("Payment started..");
    let amount = $("#payment_field").val();
    console.log(amount);
    if(amount == "" || amount == null){
      //alert("amount is required !!");
       swal("Failed!", "amount is required !!", "error");
      return;
    }

    //we will use ajax to send request  to server to create order- jquery
    $.ajax(
      {
        url:'/user/create_order',
        data:JSON.stringify({amount:amount,info: 'order_request'}),
        contentType: 'application/json',
        type: 'POST',
        dataType: 'json',
        success:function(response){
            //invoked when success
            console.log(response);
            if(response.status == "created"){
              //open payment form
              let options={
                key: "rzp_test_6K9lVshaJG2edT",
                amount: response.amount,
                currency: "INR",
                name: "Smart Contact Manager",
                description: "Donation",
                image: "https://lh3.googleusercontent.com/BBY0RXJZ3Y1jQ29ld0AotrYvVpfIRivrJ8dfzP7HOCeTX4wXMxMxdJNEvpKy-2e957yHYbCq0LKwdaL4WtgjSN_C0HVgHpIbQmleB8YwAEX7FId7LhL00TYNin8xjECibXp1HnmVn937AhGuLrQsFmd7bnkKJNUXv57_L9bl5nM-LzXia5Wudlw7KT6H2QgkQiS8mAVcWQbaQllXt6Qi4F-WthtZsOcJ5CiMIWDiPs76bw60rGrOJ_QslthQrJ_MvaHc6ZXCVIi4vhTMWApKLY6hW8J9tOR9c_2fxA8MNGI2mS4_uYBbi1m52Y6H5i9xVE-hG-4mpSa9NwmQBs1It5Gt3f5oLwcmrM5yLN9qxxrUstTq5l1M8Omv_jBWQL1QM2uRy2o66v1Ka0IyhVX05JqO6p7OulCGCjrcW2LdWzoGnQkxvOFv1UbgDDTh0F_XW-dBaXm5BLSC3UlTqlkxRvYWEN7KUOYAagWltDFA3UxRaLGoOZMMnCqKYrF5DrC-aOWUfD789xHG7KoCnHQNxBJDkqp9duzujIV5vI3oCGwoiGwGhRDbbmm7ktuAIgaK19BXGtQKNGLvJNYjd9dfFYm8yI7llaEc2IuChuRYr5R_L59jho754sykE3ulkUhUryNkJDJanOp9iMJTronk1PkWfrN1fYeijrnnNSVRzb1yvLVXhl5qOilSFEkkcy-jbVmM12RxpBiWA5JbTo9OOzQ=w515-h914-no?authuser=0",
                order_id: response.id,
                handler:function(response){
                   console.log(response.razorpay_payment_id)
                   console.log(response.razorpay_order_id)
                   console.log(response.razorpay_signature)
                   console.log('payment successful !!')
                  // alert("congrates !! Payment successful !!")

                  updatePaymentOnServer(
                      response.razorpay_payment_id,
                      response.razorpay_order_id,
                      'paid'
                      );
                },
                "prefill": {
                  "name": "",
                  "email": "",
                  "contact": ""
              },
              "notes": {
                "address": "Learn Code with Ram"
            },
            "theme": {
              "color": "#3399cc"
               },
              };

              let rzp= Razorpay(options);
              rzp.on('payment.failed', function (response){
                console.log(response.error.code);
                console.log(response.error.description);
                console.log(response.error.source);
                console.log(response.error.step);
                console.log(response.error.reason);
                console.log(response.error.metadata.order_id);
                console.log(response.error.metadata.payment_id);
                //alert("Oops Payment Failed")
                swal("Failed!", "Oops Payment Failed", "error");
        });
              rzp.open();
            }
        },
        error:function(error){

          // invoked when error
          console.log(error);
          alert("something went wrong !!")
        }
      }
    )
};

function updatePaymentOnServer(payment_id,order_id,status)
{
    $.ajax({
     url:'/user/update_order',
            data:JSON.stringify({
            payment_id:payment_id,
            order_id:order_id,
            status:status
            }),
            contentType: 'application/json',
            type: 'POST',
            dataType: 'json',
            success:function(response){
              swal("Good job!", "congrates !! Payment successful !!", "success");
            },
            error:function(error){
              swal("Failed!", "Your payment is successful, but we didi not get on server, we will contact you as soon as possible ", "error");
            },
    });
}