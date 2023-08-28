require: responseCity.js
require: city/city.sc
    module = sys.zb-common

theme: /Старт
    
    state: Start || modal = true
        q!: $regex</start>
        script:
            $session = {}
            $client = {}
            $temp = {}
            $response = {}
        a:Наконец-то достойный соперник, наша битва будет легендарной!!! Выбирай игру.
        buttons:
            "Города" -> /Города
            "Угадай число" -> /Угадчисло
            "100 спичек" -> /Стоспичек
            "Кости" -> /Кости

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
        a: Очень жаль!
        go!: /Over



    
    
    
require: slotfilling/slotFilling.sc
  module = sys.zb-common

require: common.js
    module = sys.zb-common

theme: /

    state: Угадчисло
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
            a: Ну и ладно! 
            go!: /Over
            
    state: Игра
        # сгенерируем случайное число и перейдем в стейт /Проверка
        script:
            $session.number = $jsapi.random(100) + 1;
            $session.pop = 0;
            $session.popit = 10;
            //$reactions.answer("Загадано {{$session.number}}");
            $reactions.transition("/Игра/Проверка");

        state: Проверка
            intent: /Число
            script:
                var num = $parseTree._Number;
                $session.pop += 1
                if ($session.pop < $session.popit)
                {
                    //проверяем угадал ли пользователь загаданное число и выводим соответствующую реакцию
                    if (num == $session.number) {
                        $reactions.answer("Ты выиграл! Хочешь еще раз?");
                        $reactions.transition("/Угадчисло/Согласен?");
                    }
                    else
                        if (num < $session.number) {
                            $reactions.answer(selectRandomArg(["Мое число больше!", "Бери выше", "Попробуй число больше"]));
                            $reactions.answer("Попытка номер " + $session.pop);
                        }
                        else {
                            $reactions.answer(selectRandomArg(["Мое число меньше!", "Подсказка: число меньше", "Дам тебе еще одну попытку! Мое число меньше."]));
                            $reactions.answer("Попытка номер " + $session.pop);
                        }
                            
                }
                else 
                    $reactions.answer("ОЙ, твои потытки кончились, ты проиграл")

                    
            buttons:
                    "Начать заново" -> /Угадчисло
                    "Закончить игру" -> /Over    


theme: /
    
    state: Стоспичек
        a: Правила игра: два игрока поочередно берут из общей кучи, которая составляет 100 спичек, от 1-10 штук, выигрывает тот, кто возьмёт последнюю спичку.
        script:
            $session.spichki = 100;
        buttons:
            "Компьютер" -> /Comp
            "Пользователь" -> /Wod
            
    state: Wod
        a: Выберай число.
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
                $temp.bot = 0; 
                if ($session.spichki > 20)
                {
                    $temp.bot = $reactions.random(10) + 1;
                }
                else if ($session.spichki >= 11)
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
                }
                else $temp.bot = $session.spichki;
                if ($temp.bot != 0) 
                {
                    $reactions.answer('Бот выбрал {{$temp.bot}}');
                    $session.spichki -= $temp.bot;
                    $reactions.answer("Осталось {{$session.spichki}}.");
                    if ($session.spichki == 0)
                    {
                        $reactions.answer(selectRandomArg(["Победа компьютера.", "Человечество проиграло.", "Тебе меня не победить, человечишка!"]));
                        $reactions.transition("/Over");
                    }
                }
                else
                {
                    $reactions.answer(selectRandomArg(["Ты победил.", "Я разбит, признаю поражение...", "Что ты наделал, глупец, я хотел спасти мир... "]));
                    $reactions.transition("/Over");
                }
                
    
            buttons:
                "Начать заново" -> /Стоспичек
                "Закончить игру" -> /Over 
                
    state: NOP
        event: noMatch
        go!: /Wod
            
            
                
    state: Comp
        script:
            $reactions.answer("Я хожу первый? Сам напросился")
            $temp.com = $reactions.random(10) + 1;
            $session.spichki -= $temp.com;
            $reactions.answer("Бот взял {{$temp.com}}, осталось {{$session.spichki}}");
            $reactions.transition("/Wod");
        


theme: / 
    state: Кости
        a: Игрок и компьютер бросают кубик два раза, у кого сумма чисел будет больше, тот и победил. Готов?
        
        state: Первыйход
            q: (Да/Готов/Согласен)
            script:
                $reactions.answer('Твой ход.');
                $session.kub1 = $reactions.random(6) + 1;
                $reactions.answer('Первый кубик равен {{$session.kub1}}');
                $reactions.timeout({interval: '2 seconds', targetState: '/Кости/Второйход'});
        state: Второйход
            script:
                $session.kub2 = $reactions.random(6) + 1;
                $reactions.answer('Второй кубик равен {{$session.kub2}}');
                $reactions.timeout({interval: '2 seconds', targetState: '/Кости/Третийход'});
        state: Третийход
            script:
                $session.sum1 = $session.kub1 + $session.kub2;
                $reactions.answer('Сумма кубиков равна {{$session.sum1}}');
                $reactions.timeout({interval: '2 seconds', targetState: '/Кости/Четвертыйход'});
        state: Четвертыйход
            script:
                $reactions.answer('Мой ход.');
                $temp.kub3 = $reactions.random(6) + 1;
                $reactions.answer('Первый кубик равен {{$temp.kub3}}');
                $reactions.timeout({interval: '2 seconds', targetState: '/Кости/Пятыйход'});
        state: Пятыйход
            script:
                $temp.kub4 = $reactions.random(6) + 1;
                $reactions.answer('Второй кубик равен {{$temp.kub4}}');
                $reactions.timeout({interval: '2 seconds', targetState: '/Кости/Шестойход'});
        state: Шестойход
            script:
                $session.sum2 = $temp.kub3 + $temp.kub4;
                $reactions.answer('Сумма кубиков равна {{$session.sum2}}');
                if ($session.sum1 > $session.sum2)
                    $reactions.answer('Твоё число больше, ты победил.');
                else 
                    $reactions.answer('Моё число больше, я победил.');
                $reactions.transition("/Over");
                
                