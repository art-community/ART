package ru.art.test.specification.json


import spock.lang.Specification

import static ru.art.core.constants.StringConstants.*
import static ru.art.json.descriptor.JsonEntityReader.readJson
import static ru.art.json.descriptor.JsonEntityWriter.writeJson


class JsonSpecification extends Specification {
    def "should correctly parse/write json"() {
        setup:
        def json =
                """
{
  "result": [
    {
      "data": [
        {
          "key": "ФИО",
          "value": "ЮРОВА АНАСТАСИЯ ВЯЧЕСЛАВОВНА"
          
        },
        {
          "key": "Балланс",
          "value": "-2084.06"
          
        },
        {
          "key": "Логин PPPoE",
          "value": "qcnsua6p5u"
          
        }
      ],
      "type": "kv",
      "title": "Информация о клиенте"
    },
    {
      "data": [
        [
          {
            "key": "Имя сервиса",
            "value": "i0011251363"
            
          },
          {
            "key": "Тип сервиса",
            "value": "INTERNET"
            
          },
          {
            "key": "Тарифный план",
            "value": "[РТК] xPON Всё вместе \\"Для начала\\" МОНО 100Мбит/с"
            
          },
          {
            "key": "Скорость по тарифу [Мбит/сек]",
            "value": "100"
            
          }
        ]
      ],
      "type": "table",
      "title": "Информация об услугах"
    },
    {
      "data": [
        {
          "key": "Данные ТУ",
          "value": "RST-TCN-GIRNOV-PON1-5608-001-013_01-06 [] волокно волокно41-48/43"
          
        },
        {
          "key": "Оборудование ТУ",
          "value": "RST-TCN-GIRNOV-PON1-5608-001-013_01-06"
          
        },
        {
          "key": "Порт ТУ",
          "value": "41/48/43"
          
        },
        {
          "key": "Авторизован с",
          "value": "RST-TCN-GIRNOV-PON1-5608 xpon 0/1/0/1:1.1.2000"
          
        },
        {
          "key": "Оборудование последней мили",
          "value": "RST-TCN-GIRNOV-PON1-5608"
          
        },
        {
          "key": "Порт последней мили",
          "value": "0/1/1"
          
        }
      ],
      "type": "kv",
      "title": "Технический учет"
    },
    {
      "data": [
        {
          "key": "Число сессий за последний час",
          "value": "0"
          
        },
        {
          "key": "Число сессий за последние 3 часа",
          "value": "0"
          
        },
        {
          "key": "Число сессий за последние сутки",
          "value": "3"
          
        },
        {
          "key": "Число сессий за последние 3-ое суток",
          "value": "3"
          
        }
      ],
      "type": "kv",
      "title": "Статистика сессий"
    },
    {
      "data": [
        {
          "key": "Логин",
          "value": "qcnsua6p5u"
          
        },
        {
          "key": "IP",
          "value": "46.61.84.219"
          
        },
        {
          "key": "Начало",
          "value": "2020-02-25T08:17:29.000Z"
          
        },
        {
          "key": "Конец",
          "value": "2020-02-28T08:17:30.000Z"
          
        },
        {
          "key": "Активна",
          "value": "Нет",
          "estimate": "2"
        }
      ],
      "type": "kv",
      "title": "Последняя сессия"
    },
    {
      "data": [
        {
          "key": "Cостояние платы",
          "value": "работает коррекно",
          "estimate": "0"
        },
        {
          "key": "Административное состояние порта",
          "value": "включен",
          "estimate": "0"
        },
        {
          "key": "Оперативное состояние порта",
          "value": "включен",
          "estimate": "0"
        },
        {
          "key": "Статус подключения ONT",
          "value": "отключен",
          "estimate": "1"
        },
        {
          "key": "Описание",
          "value": "auto-add-Mon-Jan-13-12-10-18-202"
        },
        {
          "key": "Длина линии [km]",
          "value": "0"
        },
        {
          "key": "Затухание [dB]",
          "value": "0",
          "estimate": "0"
        },
        {
          "key": "SN",
          "value": "5A544547C8BB20DF (ZTEG-C8BB20DF)"
        }
      ],
      "type": "kv",
      "title": "Состояние порта"
    },
    {
      "data": [],
      "type": "table",
      "title": "Таблица MAC-адресов"
    },
    {
      "data": [
        []
      ],
      "type": "table1",
      "title": "Порты ONT"
    }
  ],
  "request_id": "6778335",
  "conclusions": [
    {
      "conclusion_text": "Данные ТУ получены обоими методами но не равны.",
      "conclusion_level": 2,
      "conclusion_title": "Технический учет"
    }
  ],
  "request_created": "2020-05-12T19:43:49+0000"
}"""
        when:
        def value = readJson(json)

        then:
        writeJson(value).replaceAll(NEW_LINE, EMPTY_STRING).replaceAll(SPACE, EMPTY_STRING) == json.replaceAll(NEW_LINE, EMPTY_STRING).replaceAll(SPACE, EMPTY_STRING)
    }

}