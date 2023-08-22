require: slotfilling/slotFilling.sc
  module = sys.zb-common
require: play.sc

theme: /
    
    state: Start || modal = true
        q!: $regex</start>
        a: Привет, я чат-бот. Умею немного, но хоть что-то, если хочешь узнать что я умею, спроси.
        
    state: Bye
        intent!: /пока
        a: Пока пока

    state: NoMatch || noContext = true
        event!: noMatch
        random:
            a: Я не понял.
            a: Что вы имеете в виду?
            a: Ничего не пойму
            
    state: Match
        event!: match
        a: Щас настрою себя... Готов, говори человечишка.{{$context.intent.answer}}
        
    state: Russia
        intent!: /ярусский
        a: Извините, но я понимаю только русский язык...
        
    state: Prog
        intent!: /создатель
        random:
            a: Ты его не знаешь и никогда не узнаешь.
            a: Тот, кто создал ИИ в фильме терминатор.
            a: Ну уж точно не ты ,человечишка!!!
            
    state: Play
        intent!: /LetsPlay
        go!: /Старт/Start
        
    state: Over
        random:
            a: GG
            a: Классно поиграли, зови ещё.
            
    state: Skills
        intent!: /Умения
        a: Я умею играть в игры "Города" и "Угадай число". Также есть связь с богами, они мне дают информацию о погоде в любом городе (ну почти).
    
    
require: functions.js

theme: /

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
        event: noMatch
        a: Давай заного, только с городом
        go: /GetWeather
        
    

