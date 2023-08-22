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
            "100 спичек" -> /Wod

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
        q: $regex</start>  
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
            go!: /Over
            
    state: Игра
        # сгенерируем случайное число и перейдем в стейт /Проверка
        script:
            $session.number = $jsapi.random(100) + 1;
            //$reactions.answer("Загадано {{$session.number}}");
            $reactions.transition("/Проверка");

    state: Проверка
        #q: @duckling.number
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
                }
                else 
                    $reactions.answer(selectRandomArg(["Мое число меньше!", "Подсказка: число меньше", "Дам тебе еще одну попытку! Мое число меньше."]));
                
        buttons:
                "Начать заново" -> /Угадчисло
                "Закончить игру" -> /Over    


theme: /
    
    state: Стоспичек
        #q!: * (Сто/100) спичек *
        a: Правила игра: два игрока поочередно берут из общей кучи, которая составляет 100 спичек, от 1-10 штук, выигрывает тот, кто возьмёт последнюю спичку.
        go: /Wod
        #buttons:
            #"Компьютер" -> /Comp
            #"Пользователь" -> /Use
            
    state: Wod
        a: Правила игра: два игрока поочередно берут из общей кучи, которая составляет 100 спичек, от 1-10 штук, выигрывает тот, кто возьмёт последнюю спичку.
        a: Ты первый выберашь число.
        script:
            $session.spichki = 100;
        state: Oper
            q: (1/2/3/4/5/6/7/8/9/10) 
            script: 
                var a = $parseTree.text;
                $session.spichki -= a;
                $reactions.answer("Осталось {{$session.spichki}}.");
                if ($session.spichki == 0)
                {
                    $reactions.answer(selectRandomArg(["Ты победил.", "Я разбит, признаю поражение...", "Что ты наделал, глупец, я хотел спасти мир... "]));
                    $reactions.transition("/Over");
                }
                if ($session.spichki)
                {
                    $temp.bot = 0; 
                    if ($session.spichki > 20)
                    {
                        $temp.bot = $reactions.random(10) + 1;
                        $reactions.answer('Бот выбрал {{$temp.bot}}');
                    }
                    else 
                    {
                        switch ($session.spichki)
                        {
                            case(20):
                                $temp.bot = 9;
                                break;
                            case(19):
                                $temp.bot = 8;
                                break;
                            case(18):
                                $temp.bot = 7;
                                break;
                            case(17):
                                $temp.bot = 6;
                                break;
                            case(16):
                                $temp.bot = 5;
                                break;
                            case(15):
                                $temp.bot = 4;
                                break;
                            case(14):
                                $temp.bot = 3;
                                break;
                            case(13):
                                $temp.bot = 2;
                                break;
                            case(12):
                                $temp.bot = 1;
                                break;
                            case(11):
                                $reactions.answer(selectRandomArg(["А ты это ловко придумал, я даже в начале непонял, глупец.", "Вот бы как в былые времена, взять 0..."]))
                                $temp.bot = $reactions.random(10) + 1;
                                break;
                            }
                            $reactions.answer('Бот выбрал {{$temp.bot}}');
                    }
                    $session.spichki -= $temp.bot ;
                    $reactions.answer("Осталось {{$session.spichki}}.");
                    if ($session.spichki == 0)
                    {
                        $reactions.answer(selectRandomArg(["Победа компьютера.", "Человечество проиграло.", "Тебе меня не победить, человечишка!"]));
                        $reactions.transition("/Over");
                    }
                }
                
        state: NOP
            event: noMatch
            go!: /Wod
            
            
                
    state: Comp
        script:
            /*if (a <= 10 && a != 0 && a>=1) {
                    $session.spichki -= a;
                    if ($session.spichki = 0){
                        $reactions.answer(selectRandomArg(["Ты победил.", "Я разбит, признаю поражение...", "Что ты наделал, глупец, я хотел спасти мир... "]));
                        $reactions.transition("/Over");
                    }
                }
                else 
                    $reactions.answer(selectRandomArg(["Твое число может быть от 1 до 10!", "Ты правила вообще читал?", "Попробуй ещё раз."]));
                    
                var b = 0;
                if ($session.spichki > 20){
                 b = Math.random(1,10);
                 $reactions.answer("Бот выбрал {{b}}");
                }
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
                if ($session.spichki = 0)
                {
                    $reactions.answer(selectRandomArg(["Победа компьютера.", "Человечество проиграло.", "Тебе меня не победить, человечишка!"]));
                    $reactions.transition("/Over");

            
        
            
