require: responseCity.js
require: city/city.sc
    module = sys.zb-common

theme: /Старт
    
    state: Start || modal = true
        q!: $regex</start>
        a:Наконец-то достойный соперник, наша битва будет легендарной!!! Выбирай игру.
        buttons:
            "Города" -> /Города
            "Угадай число" -> /Угадчисло

theme: /
    
    state: /Города || modal = true
        q!: $regex</start>
        script:
            // обнуление всех сессионных переменных
            $session = {}
            $client = {}
            $temp = {}
            $response = {}
        a: Кто загадывает город: компьютер или пользователь?

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
        go!: /Over



        
        
        
require: slotfilling/slotFilling.sc
  module = sys.zb-common

require: common.js
    module = sys.zb-common

theme: /

    state: Угадчисло
        q!: $regex</start>
        intent!: /Давай поиграем
        a: Игра больше-меньше. Загадаю число от 0 до 100, ты будешь отгадывать. Начали!!!
        go!: /Угадчисло/Согласен?
        
        state: Согласен?

        state: Да
            intent: /Согласие
            go!: /Игра

        state: Нет
            intent: /Несогласие
            a: Ну и ладно! Если передумаешь — скажи "давай поиграем"
            
    state: Игра
        # сгенерируем случайное число и перейдем в стейт /Проверка
        script:
            $session.number = $jsapi.random(100) + 1;
            # $reactions.answer("Загадано {{$session.number}}");
            $reactions.transition("/Проверка");

    state: Проверка
        q!: $regex</Проверка>
        intent: /Число
        script:
            
            i++
            if (i<5)
                $reactions.transition("/Угадчисло/Согласен?");
            else
            {
                var num = $parseTree._Number;
            # проверяем угадал ли пользователь загаданное число и выводим соответствующую реакцию
                if (num == $session.number) {
                    $reactions.answer("Ты выиграл! Хочешь еще раз?");
                    $reactions.transition("/Угадчисло/Согласен?");
                }
                else
                    if (num < $session.number) {
                        $reactions.answer(selectRandomArg(["Мое число больше!", "Бери выше", "Попробуй число больше"]));
                        //$reactions.transition("/ПопыткаII");
                    }
                    
                    else {
                        $reactions.answer(selectRandomArg(["Мое число меньше!", "Подсказка: число меньше", "Дам тебе еще одну попытку! Мое число меньше."]));
                        //$reactions.transition("/ПопыткаII");
                    }
            }
            
            
                
    state: ПопыткаII
        q!: $regex</ПопыткаII>
        a: Вторая попытка
        script:
            var num = $parseTree._Number;
            # проверяем угадал ли пользователь загаданное число и выводим соответствующую реакцию
            if (num == $session.number) {
                $reactions.answer("Ты выиграл! Хочешь еще раз?");
                $reactions.transition("/Угадчисло/Согласен?");
            }
            else
                if (num < $session.number) {
                    $reactions.answer(selectRandomArg(["Мое число больше!", "Бери выше", "Попробуй число больше"]));
                    $reactions.transition("/Угадчисло");
                }
                else {
                    $reactions.answer(selectRandomArg(["Мое число меньше!", "Подсказка: число меньше", "Дам тебе еще одну попытку! Мое число меньше."]));
                    $reactions.transition("/Угадчисло");
                }
           