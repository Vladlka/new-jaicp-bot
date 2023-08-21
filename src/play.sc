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
            "100 спичек" -> /Стоспичек

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
        buttons:
            "Компьютер" -> /Города/Computer
            "Пользователь" -> /Города/User
        

        state: User
            intent: /user
            a: Назовите город
            script:
                $session.keys = Object.keys($Cities);
                $session.prevBotCity = 0;
            buttons:
                "Начать заново" -> /Города
                "Закончить игру" -> /Over
            go!: /LetsPlayCitiesGame

        state: Computer
            intent: /computer
            script:
                $session.keys = Object.keys($Cities);
                var city = $Cities[chooseRandCityKey($session.keys)].value.name
                $reactions.answer(city)
                $session.prevBotCity = city
            buttons:
                "Начать заново" -> /Города
                "Закончить игру" -> /Over
            go!: /LetsPlayCitiesGame

        state: LocalCatchAll
            event: noMatch
            a: Это не похоже на ответ. Попробуйте еще раз.
            buttons:
                "Начать заново" -> /Города
                "Закончить игру" -> /Over

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
            buttons:
                "Начать заново" -> /Города
                "Закончить игру" -> /Over

        state: NoMatch
            event: noMatch
            a: Я не знаю такого города. Попробуйте ввести другой город
            buttons:
                "Начать заново" -> /Города
                "Закончить игру" -> /Over

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
        a: Игра больше-меньше. Загадаю число от 0 до 100, ты будешь отгадывать. Как будешь готов, напиши.
        go!: /Угадчисло/Согласен?
        
        state: Согласен?

        state: Да
            intent: /Согласие
            script: 
            go!: /Игра

        state: Нет
            intent: /Несогласие
            a: Ну и ладно! Если передумаешь — скажи "давай поиграем"
            
    state: Игра
        # сгенерируем случайное число и перейдем в стейт /Проверка
        script:
            $session.number = $jsapi.random(100) + 1;
            //$reactions.answer("Загадано {{$session.number}}");
            $reactions.transition("/Проверка");

    state: Проверка
        q!: $regex</Проверка>
        intent: /Число
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
                    //$reactions.transition("/ПопыткаII");
                }
                else {
                    $reactions.answer(selectRandomArg(["Мое число меньше!", "Подсказка: число меньше", "Дам тебе еще одну попытку! Мое число меньше."]));
                    //$reactions.transition("/ПопыткаII");
                }
        buttons:
                "Начать заново" -> /Угадчисло
                "Закончить игру" -> /Over    


theme: /
    
    state: Стоспичек
        q!: $regex</start>
        script:
            $session.spichki = 100;
            $response.replies = $response.replies || [];
        a: Правила игра: два игрока поочередно берут из общей кучи, которая составляет 100 спичек, от 1-10 штук, выигрывает тот, кто возьмёт последнюю спичку. Кто начитнает? 
        buttons:
            "Компьютер" -> /Comp
            "Пользователь" -> /Use

            
    state: Use
        intent: /Спички
        script:
            //$response.replies = $response.replies || [];
            $reactions.answer("Осталось {{$session.spichki}}");
        random:
            a: Вводи число...
            a: Твое число...
        script: 
            var a = $parseTree._Number;
            if (a <= 10 && a != 0 && a>=1) {
                $session.spichki -= a
                if ($session.spichki = 0){
                    $reactions.answer(selectRandomArg(["Ты победил.", "Я разбит, признаю поражение...", "Что ты наделал, глупец, я хотел спасти мир... "]));
                    $reactions.transition("/Over");
                }
                    
            $reactions.transition("/Comp");
            }
            else {
                $reactions.answer(selectRandomArg(["Твое число может быть от 1 до 10!", "Ты правила вообще читал?", "Попробуй ещё раз."]));
                $reactions.transition("/Use");
                }
                
    state: Comp
        script:
            //$response.replies = $response.replies || [];
            $reactions.answer("Осталось {{$session.spichki}}");
            var b = 0;
            if ($session.spichki > 20)
             b = Math.random(1,10);
            else 
            {
                if ($session.spichki <= 20 && $session.spichki > 10)
                {
                 for (var i = 1; i<=10;i++)
                 {
                     if (($session.spichki - i) == 11)
                     {
                         b = i;
                         break;
                     }
                 }
                }
                else 
                    b = $session.spichki;
            }
            $session.spichki -= b
            if ($session.spichki = 0){
                $reactions.answer(selectRandomArg(["Победа компьютера.", "Человечество проиграло.", "Тебе меня не победить, человечишка!"]));
                $reactions.transition("/Over");
                }
            else $reactions.transition("/Use")
            
        
            
