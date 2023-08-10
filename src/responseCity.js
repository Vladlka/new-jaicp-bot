function responseCity(parseTree, keys, cities) {
    // сохраняем введенный город
    var city = parseTree._City.name
    // берем последнюю букву города
    var letter = city[city.length - 1]
    // будем присваивать переменной ключ найденного города
    var response = 0
    
    /* пройдемся по элементам списка городов пока не найдем город,
    который начинается на последнюю букву предыдущего города*/
    keys.forEach(function(elem) {
        if (cities[elem].value.name[0].toLowerCase() == letter) {
            response = elem
        }
    })
    
    /* если города, начинающиеся с определенной буквы закончились,
    то используем вторую букву с конца */
    if (response == 0) {
        letter = city[city.length - 2]
        keys.forEach(function(elem) {
        if (cities[elem].value.name[0].toLowerCase() == letter) {
            response = elem
        }
    }) 
    }
    return response
}

/* будем вызывать эту функцию из сценария
для удаления введенного города из всего списка */
function findByName(city, keys, cities) {
    // будем хранить ключ найденного города
    var response = 0
    var i = 0
    
    // пройдемся по элементам списка городов пока не найдем введенный город
    keys.forEach(function(elem) {
        if (cities[elem].value.name == city) {
            response = i
        }
        i++
    })
    
    return response
}

function checkCity(parseTree, keys, cities) {
    // сохраняем введенный город
    var city = parseTree._City.name
    var response = false

    // пройдемся по элементам списка городов пока не найдем введенный город
    keys.forEach(function(elem) {
        if (cities[elem].value.name == city) {
            response = true
        }
    })
    return response;
}

function checkLetter(playerCity, botCity) {
    var response = false
    /* выводим true, если первая буква введенного пользователем города
    совпадает с последней буквой введенного ботом города  */
    if ((playerCity[0].toLowerCase() == botCity[botCity.length - 1]) 
        || (playerCity[0].toLowerCase() == botCity[botCity.length - 2])) {
        response = true
    }
    return response
}

function chooseRandCityKey(keys) {
    var i = 0
    // посчитаем количество элементов в списке городов
    keys.forEach(function(elem) {
        i++
    })
    // выведем случайное значение
    return  $jsapi.random(i);
}
// проверка на полное название города
function isAFullNameOfCity() {
    return $jsapi.context().parseTree._City.name.toUpperCase() == $jsapi.context().parseTree.text.toUpperCase();
}