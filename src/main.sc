require: slotfilling/slotFilling.sc
  module = sys.zb-common
require: play.sc

theme: /
    
    state: Start || modal = true
        q!: $regex</start>
        a: Привет, я чат-бот. Умею немного, но хоть что-то, если хочешь узнать что я умею, спроси.
        buttons:
            "Игры" -> /Старт
            "Погода" -> /GetWeather
            "Конвертирование валют" -> /Количество
    
    
    state: Naw
        a: Мы в главном меню, чего тебе нужно?
        buttons:
            "Игры" -> /Старт
            "Погода" -> /GetWeather
            "Конвертирование валют" -> /Количество

        
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
        go: /Naw
            
    state: Skills
        intent!: /Умения
        a: Я умею играть в игры "Города", "Угадай число" и "100 спичек". Также есть связь с богами, они мне дают информацию о погоде в любом городе (ну почти).
    
    
require: functions.js

theme: /
    state: GetWeather
        intent!: /geo
        script:
            var city = $caila.inflect($parseTree._geo, ["nomn"]);
            openWeatherMapCurrent("metric", "ru", city).then(function (res) {
                if (res && res.weather) {
                    $reactions.answer("Сегодня в городе " + capitalize(city) + " " + res.weather[0].description + ", " + Math.round(res.main.temp) + "°C");
                    if(res.weather[0].main == 'Rain' || res.weather[0].main == 'Drizzle') {
                        $reactions.answer("Советую захватить с собой зонтик!")
                    } else if (Math.round(res.main.temp) < 0) {
                        $reactions.answer("Бррррр ну и мороз")
                    }
                    $reactions.transition("/Naw")
                } else {
                    $reactions.answer("Что-то сервер барахлит. Не могу узнать погоду.");
                    $reactions.transition("/Naw")
                }
            }).catch(function (err) {
                $reactions.answer("Что-то сервер барахлит. Не могу узнать погоду.");
                $reactions.transition("/Naw")
            });
            

                

    state: CatchAll || noContext=true
        event: noMatch
        a: Давай заного, только с городом
        go: /GetWeather
        
        
        
        
require: money.js

theme: /

    state: Количество
        intent!: /Валюта
        InputNumber:
            prompt = Какое количество единиц вы бы хотели конвертировать?
            failureMessage = ["Не могли бы вы попробовать еще раз?", "Пожалуйста, введите число в диапазоне не отрицательное."]
            minValue = 1
            maxValue = 100000000000
            varName = amount
            then = /Изчего
    
    state: Изчего
        InputText:
            varName = from
            prompt = Введи название валюты из которой хочешь конвертировать, например: RUB - рубли, EUR - евро...
            then = /Вочто
        script:
            //$session.from = $parseTree.text;

    state: Вочто
        InputText:
            varName = to
            prompt = Введи название валюты из которой хочешь конвертировать, например: RUB - рубли, EUR - евро...
            then = /Конверт
        script:
            //$session.to = $parseTree.text;
            
    state: Конверт
        script:
            var result = moneyConvert($session.from, $session.to, $session.amount)
            $reactions.answer("На сегодняшнее число " + result.data.date + " ты конвертировал из " + $session.from + " в "+ $session.to + " в количестве "+ $session.amount+ " единиц. В итоге получилось "+ result.data.result.toFixed(2))