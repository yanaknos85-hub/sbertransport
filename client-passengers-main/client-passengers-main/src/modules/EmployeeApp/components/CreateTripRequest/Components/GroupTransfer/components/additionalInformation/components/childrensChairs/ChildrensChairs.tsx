import '../../../../override.scss';
import { Form, InputNumber } from 'antd';
import { observer } from 'mobx-react';
import React, { FC } from 'react';

import childrensChairsStyles from './chairs.module.scss';
import { fieldTitles } from '../../../../constants';
import Minus from 'shared/components/Images/Minus.svg';
import Plus from 'shared/components/Images/Plus.svg';
import { ValidationRules } from 'shared/fieldValidationRules';

export const ChildrensChairs: FC = observer(
  (): JSX.Element => {
    return (
      <div className={childrensChairsStyles.wrapper_childrensChairs}>
        <Form.List name="childSeatDetails">
          {() => (
            <>
              <Form.Item
                shouldUpdate={true}
              >
                {({ getFieldValue }) => {
                  return (
                    <>
                      <div className={childrensChairsStyles.wrapper_item_childrensChairs}>
                        <div className={childrensChairsStyles.type_childrensChair}>
                          <span>
                            Кресло, от 9 мес. до 4 лет
                          </span>
                          <span>
                            9-18 кг
                          </span>
                        </div>
                        <Form.Item
                          key={fieldTitles.group1}
                          name={fieldTitles.group1}
                          rules={[ValidationRules.general.isValidChildSeat(getFieldValue('information'))]}
                        >
                          <InputNumber
                            max={999}
                            type="number"
                            className="numberCost"
                            formatter={value => Number(value).toFixed()}
                            controls={{
                              upIcon: <img src={Plus} alt="plus" />,
                              downIcon: <img src={Minus} alt="minus" />,
                            }}
                          />
                        </Form.Item>
                      </div>
                      <div className={childrensChairsStyles.wrapper_item_childrensChairs}>
                        <div className={childrensChairsStyles.type_childrensChair}>
                          <span>
                            Кресло, 3-7 лет
                          </span>
                          <span>
                            12-15 кг
                          </span>
                        </div>
                        <Form.Item
                          key={fieldTitles.group2}
                          name={fieldTitles.group2}
                          rules={[ValidationRules.general.isValidChildSeat(getFieldValue('information'))]}
                        >
                          <InputNumber
                            max={999}
                            type="number"
                            className="numberCost"
                            formatter={value => Number(value).toFixed()}
                            controls={{
                              upIcon: <img src={Plus} alt="plus" />,
                              downIcon: <img src={Minus} alt="minus" />,
                            }}
                          />
                        </Form.Item>
                      </div>
                      <div className={childrensChairsStyles.wrapper_item_childrensChairs}>
                        <div className={childrensChairsStyles.type_childrensChair}>
                          <span>
                            Бустер, 6-12 лет
                          </span>
                          <span>
                            22-36 кг
                          </span>
                        </div>
                        <Form.Item
                          key={fieldTitles.booster}
                          name={fieldTitles.booster}
                          rules={[ValidationRules.general.isValidChildSeat(getFieldValue('information'))]}
                        >
                          <InputNumber
                            max={999}
                            type="number"
                            className="numberCost"
                            formatter={value => Number(value).toFixed()}
                            controls={{
                              upIcon: <img src={Plus} alt="plus" />,
                              downIcon: <img src={Minus} alt="minus" />,
                            }}
                          />
                        </Form.Item>
                      </div>
                      <div className={childrensChairsStyles.wrapper_item_childrensChairs}>
                        <div className={childrensChairsStyles.type_childrensChair}>
                          <span>
                            Люлька, до 1 года
                          </span>
                          <span>
                            до 13 кг
                          </span>
                        </div>
                        <Form.Item
                          key={fieldTitles.newborn}
                          name={fieldTitles.newborn}
                          rules={[ValidationRules.general.isValidChildSeat(getFieldValue('information'))]}
                        >
                          <InputNumber
                            max={999}
                            type="number"
                            className="numberCost"
                            formatter={value => Number(value).toFixed()}
                            controls={{
                              upIcon: <img src={Plus} alt="plus" />,
                              downIcon: <img src={Minus} alt="minus" />,
                            }}
                          />
                        </Form.Item>
                      </div>
                    </>
                  );
                }}
              </Form.Item>
            </>
          )}
        </Form.List>
      </div>
    );
  }
);
