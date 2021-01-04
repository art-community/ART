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
          "key": "���",
          "value": "����� ��������� ������������"
          
        },
        {
          "key": "�������",
          "value": "-2084.06"
          
        },
        {
          "key": "����� PPPoE",
          "value": "qcnsua6p5u"
          
        }
      ],
      "type": "kv",
      "title": "���������� � �������"
    },
    {
      "data": [
        [
          {
            "key": "��� �������",
            "value": "i0011251363"
            
          },
          {
            "key": "��� �������",
            "value": "INTERNET"
            
          },
          {
            "key": "�������� ����",
            "value": "[���] xPON �� ������ \\"��� ������\\" ���� 100����/�"
            
          },
          {
            "key": "�������� �� ������ [����/���]",
            "value": "100"
            
          }
        ]
      ],
      "type": "table",
      "title": "���������� �� �������"
    },
    {
      "data": [
        {
          "key": "������ ��",
          "value": "RST-TCN-GIRNOV-PON1-5608-001-013_01-06 [] ������� �������41-48/43"
          
        },
        {
          "key": "������������ ��",
          "value": "RST-TCN-GIRNOV-PON1-5608-001-013_01-06"
          
        },
        {
          "key": "���� ��",
          "value": "41/48/43"
          
        },
        {
          "key": "����������� �",
          "value": "RST-TCN-GIRNOV-PON1-5608 xpon 0/1/0/1:1.1.2000"
          
        },
        {
          "key": "������������ ��������� ����",
          "value": "RST-TCN-GIRNOV-PON1-5608"
          
        },
        {
          "key": "���� ��������� ����",
          "value": "0/1/1"
          
        }
      ],
      "type": "kv",
      "title": "����������� ����"
    },
    {
      "data": [
        {
          "key": "����� ������ �� ��������� ���",
          "value": "0"
          
        },
        {
          "key": "����� ������ �� ��������� 3 ����",
          "value": "0"
          
        },
        {
          "key": "����� ������ �� ��������� �����",
          "value": "3"
          
        },
        {
          "key": "����� ������ �� ��������� 3-�� �����",
          "value": "3"
          
        }
      ],
      "type": "kv",
      "title": "���������� ������"
    },
    {
      "data": [
        {
          "key": "�����",
          "value": "qcnsua6p5u"
          
        },
        {
          "key": "IP",
          "value": "46.61.84.219"
          
        },
        {
          "key": "������",
          "value": "2020-02-25T08:17:29.000Z"
          
        },
        {
          "key": "�����",
          "value": "2020-02-28T08:17:30.000Z"
          
        },
        {
          "key": "�������",
          "value": "���",
          "estimate": "2"
        }
      ],
      "type": "kv",
      "title": "��������� ������"
    },
    {
      "data": [
        {
          "key": "C�������� �����",
          "value": "�������� ��������",
          "estimate": "0"
        },
        {
          "key": "���������������� ��������� �����",
          "value": "�������",
          "estimate": "0"
        },
        {
          "key": "����������� ��������� �����",
          "value": "�������",
          "estimate": "0"
        },
        {
          "key": "������ ����������� ONT",
          "value": "��������",
          "estimate": "1"
        },
        {
          "key": "��������",
          "value": "auto-add-Mon-Jan-13-12-10-18-202"
        },
        {
          "key": "����� ����� [km]",
          "value": "0"
        },
        {
          "key": "��������� [dB]",
          "value": "0",
          "estimate": "0"
        },
        {
          "key": "SN",
          "value": "5A544547C8BB20DF (ZTEG-C8BB20DF)"
        }
      ],
      "type": "kv",
      "title": "��������� �����"
    },
    {
      "data": [],
      "type": "table",
      "title": "������� MAC-�������"
    },
    {
      "data": [
        []
      ],
      "type": "table1",
      "title": "����� ONT"
    }
  ],
  "request_id": "6778335",
  "conclusions": [
    {
      "conclusion_text": "������ �� �������� ������ �������� �� �� �����.",
      "conclusion_level": 2,
      "conclusion_title": "����������� ����"
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