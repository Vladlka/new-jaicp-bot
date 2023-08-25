var requestOptions = {
  method: 'GET',
  redirect: 'follow',
  headers: myHeaders
};

function moneyConvert(to, from, amount){
    var money_API_KEY = $env.get('api_money');
return $http.get("https://api.apilayer.com/fixer/convert?to=$to&from=$from&amount=$amount, requestOptions {
        timeout: 10000,
        query:{
            APPID: money_API_KEY,
            to: to,
            from: from,
            amount: amount
        }
    });
}