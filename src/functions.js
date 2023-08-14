function openWeatherMapCurrent(units, lang, q){
    var OPENWEATHERMAP_API_KEY = $env.get('api_key');
return $http.get("http://api.openweathermap.org/data/2.5/weather?APPID=${APPID}&units=${units}&lang=${lang}&q=${q}", {
        timeout: 10000,
        query:{
            APPID: OPENWEATHERMAP_API_KEY,
            units: units,
            lang: lang,
            q: q
        }
    });
}
