/*var requestOptions = {
  method: 'GET',
  redirect: 'follow',
  headers: myHeaders
};*/

function moneyConvert(to, from, amount){
    var money_API_KEY = "DoHVRx12ilm2yHUWxycBujTTU7Ir4fRa";
return $http.get("https://api.apilayer.com/fixer/convert?to=${to}&from=${from}&amount=${amount}", {
        timeout: 30000,
        headers: {"apikey": money_API_KEY},
        query:{
            APPID: money_API_KEY,
            to: to,
            from: from,
            amount: amount
        }
    });
}