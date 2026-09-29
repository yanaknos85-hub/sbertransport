
import {
  Button, Form, Input, Modal, Select
} from 'antd';
import React, { Dispatch, SetStateAction, useState } from 'react';

import { TTaxiClass } from 'stores/Trip/Trip.interface';

import { useRequest } from './useRequest';

export type TaxiClassesToRus = { [T in TTaxiClass]?: string };

export interface IRequestModalProps {
  onRequestClick: () => void;
  taxiClasses: TaxiClassesToRus;
}

export interface IUseRequestProps {
  setVisible: Dispatch<SetStateAction<boolean>>;
  onRequestClick: () => void;
}

export const RequestModal: React.FC<IRequestModalProps> = ({ onRequestClick, taxiClasses }): JSX.Element => {
  const { Option: OptionSelect } = Select;

  const [visible, setVisible] = useState(false);
  const { showModal, hideModal } = useRequest({ setVisible, onRequestClick });

  return (
    <div>
      <Button
        size="small"
        onClick={showModal}
        style={{ width: 141 }}
      >
        Запросить лимит
      </Button>

      <Modal
        title="Запрос лимита"
        visible={visible}
        onOk={hideModal}
        onCancel={hideModal}
        okText="Отправить на согласование"
        width={332}
        cancelButtonProps={{ style: { display: 'none' } }}
        centered={true}
      >
        <>
          <Form.Item name="transportView" label="Вид транспорта">
            <Select placeholder="Выберите предпочтения">
              {Object.values(taxiClasses).map(
                (taxiClass, key) => taxiClass && (
                // eslint-disable-next-line react/no-array-index-key
                <OptionSelect key={key} value={taxiClass}>
                  {/* FIXME react/no-array-index-key */}
                  {taxiClass}
                </OptionSelect>
                )
              )}
            </Select>
          </Form.Item>

          <Form.Item name="amount" label="Запрашиваемая сумма">
            <Input placeholder="Запрашиваемая сумма" type="number" />
          </Form.Item>
        </>
      </Modal>
    </div>
  );
};
