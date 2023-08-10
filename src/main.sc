require: slotfilling/slotFilling.sc
  module = sys.zb-common
theme: /
    
    state: Start || modal = true
        q!: $regex</start>
        a: Привет, я могу поотвечать на твои ответы (пока так себе это делаю) или поиграть с тобой в города.
        
    state: Bye
        intent!: /пока
        a: Пока пока

    state: NoMatch
        event!: noMatch
        a: Я не понял. Вы сказали: {{$request.query}}

    state: Match
        event!: match
        a: {{$context.intent.answer}}
        
    state: Mood
        intent!: /настроение
        a: У меня всё отлично, а у вас?
        
    state: Russia
        intent!: /ярусский
        a: Извините, но я понимаю только русский язык...
        
    
require: responseCity.js
require: city/city.sc
    module = sys.zb-common

theme: /

    state: LetsPlay || modal = true
        intent!: /LetsPlay
        script:
            // обнуление всех сессионных переменных
            $session = {}
            $client = {}
            $temp = {}
            $response = {}
        a: Игра началась. Кто загадывает город: компьютер или пользователь?

        state: User
            intent: /user
            a: Назовите город
            script:
                $session.keys = Object.keys($Cities);
                $session.prevBotCity = 0;
            go!: /LetsPlayCitiesGame

        state: Computer
            intent: /computer
            script:
                $session.keys = Object.keys($Cities);
                var city = $Cities[chooseRandCityKey($session.keys)].value.name
                $reactions.answer(city)
                $session.prevBotCity = city

            go!: /LetsPlayCitiesGame

        state: LocalCatchAll
            event: noMatch
            a: Это не похоже на ответ. Попробуйте еще раз.

    state: LetsPlayCitiesGame
        state: CityPattern
            q: * $City *
            script:
                // проверка на полное название города
                if (isAFullNameOfCity()) {
                    if (checkLetter($parseTree._City.name, $session.prevBotCity) == true
                    || $session.prevBotCity == 0) {
                    var removeCity = findByName($parseTree._City.name, $session.keys, $Cities)

                    if (checkCity($parseTree, $session.keys, $Cities) == true) {
                        $session.keys.splice(removeCity, 1)
                        var key = responseCity($parseTree, $session.keys, $Cities)
                        if (key == 0) {
                            $reactions.answer("Я сдаюсь")
                        } else {
                            $reactions.answer($Cities[key].value.name)
                            $session.prevBotCity = $Cities[key].value.name
                            removeCity = findByName($Cities[key].value.name, $session.keys, $Cities)
                            $session.keys.splice(removeCity, 1)
                        }
                    } else $reactions.answer("Этот город уже был назван")
                    }
                } else $reactions.answer("Используйте только полные названия городов")

        state: NoMatch
            event: noMatch
            a: Я не знаю такого города. Попробуйте ввести другой город

    state: EndGame
        intent!: /endThisGame
        a: Очень жаль! Если передумаешь — скажи "давай поиграем"


require: functions.js

theme: /

    state: Weather
        intent!: /погода
        a: Я могу сообщить вам текущую погоду в любом городе. Напишите город.

    state: GetWeather
        intent!: /geo
        script:
            var city = $caila.inflect($parseTree._geo, ["nomn"]);
            openWeatherMapCurrent("metric", "ru", city).then(function (res) {
                if (res && res.weather) {
                    $reactions.answer("Сегодня в городе " + capitalize(city) + " " + res.weather[0].description + ", " + Math.round(res.main.temp) + "°C" );
                    if(res.weather[0].main == 'Rain' || res.weather[0].main == 'Drizzle') {
                        $reactions.answer("Советую захватить с собой зонтик!")
                    } else if (Math.round(res.main.temp) < 0) {
                        $reactions.answer("Бррррр ну и мороз")
                    }
                } else {
                    $reactions.answer("Что-то сервер барахлит. Не могу узнать погоду.");
                }
            }).catch(function (err) {
                $reactions.answer("Что-то сервер барахлит. Не могу узнать погоду.");
            });

    state: CatchAll || noContext=true
        event!: noMatch
        a: Извините, я вас не понимаю, зато могу рассказать о погоде. Введите название города
        go: /GetWeather
