import React, { FC, useEffect, useState } from 'react';
import {
  Button, Form, FormInstance, Modal, Radio, Space
} from 'antd';
import FormField from 'shared/form/FormField/FormField';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useModalState } from 'shared/hooks/useModal';

import uuid from 'utils/uuid';

import * as S from './AdditionalServices.style';
import { PackageControl } from './Components/PackageControl';
import { ServiceItem } from './Components/ServiceItem';
import { formFields, serviceItems } from './constants';
import { Package, ServiceItems } from './types';

interface Props {
  form: FormInstance;
}

export const AdditionalServices: FC<Props> = props => {
  const { form } = props;

  const { cargoStore } = useAppStoreContext();

  const [items, setItems] = useState<ServiceItems[]>(serviceItems);
  const [packages, setPackages] = useState<Package[]>([]);

  useEffect(() => {
    const getPacks = async () => {
      await cargoStore.getPack();
      const serverPacks = cargoStore.packs;
      const savedPackages = cargoStore.packageForm?.packages || [];

      // Формируем массив объектов с количеством
      const loadedPackages = serverPacks.map(pack => {
        const saved = savedPackages.find(sp => sp.package === pack.id);
        return {
          ...pack,
          count: saved ? saved.packageCount : 0,
        };
      });

      setPackages(loadedPackages);
    };

    getPacks();
  }, []);

  const [modal, modalActions] = useModalState();
  const { hide } = modalActions;

  const handleChange = (name, value) => {
    // Сохраняем доп. услуги в стор при переходе по шагам
    cargoStore.setAdditionalServices({ [name]: form.getFieldValue(name) });
    setItems(prevValues => prevValues
      .map(item => item.name === name ? { ...item, value: value } : item
      ));
  };

  const handleReducePackage = (updatedItems: Package[]) => (
    updatedItems?.reduce((acc, cur) => {
      if (cur?.count && cur?.count > 0) {
        return [
          ...acc,
          {
            packageTitle: cur.name,
            package: cur.id,
            packageCount: cur.count as number,
          },
        ];
      }
      return acc;
    }, [] as { package: string; packageCount: number }[]
    )
  );

  const handleAddPackage = pkgId => {
    const existingPkg = packages.find(pkg => pkg.id === pkgId);

    if (existingPkg) {
      setPackages(prevPackages => {
        const updatedItems = prevPackages.map(pkg => {
          if (pkg.id === pkgId) {
            return { ...pkg, count: 1 };
          }
          return pkg;
        });

        const packageList = handleReducePackage(updatedItems);
        cargoStore.savePackageForm({ packages: packageList });
        return updatedItems;
      });
    }
  };

  const handleUpdatePackageQty = (pkgId, newQty) => {
    setPackages(prevPackages => {
      const updatedItems = prevPackages.map(pkg => {
        if (pkg.id === pkgId) {
          return { ...pkg, count: newQty };
        }
        return pkg;
      });

      const packageList = handleReducePackage(updatedItems);
      cargoStore.savePackageForm({ packages: packageList });
      return updatedItems;
    });
  };

  const footer = (
    <div>
      <Button onClick={hide}>Не поднимать</Button>
      <Button
        type="primary"
        onClick={hide}
      >
        Поднять
      </Button>
    </div>
  );

  useEffect(() => {
    // Сброс Swtich-а подъёма на этаж, если грузчики не нужны
    if (!form.getFieldValue('loaders')) {
      form.setFieldsValue({ lift: false });
      cargoStore.setAdditionalServices({ lift: form.getFieldValue('lift') });
    }
  }, [cargoStore.additionalServices.loaders]);

  return (
    <S.Wrapper>
      <S.Title>Дополнительные услуги</S.Title>
      <Form form={form}>
        {/* Переключатели(грузчики, транспортировка авто) */}
        {items.map(item => (
          <ServiceItem
            key={uuid()}
            item={item}
            onChange={value => handleChange(item.name, value)}
            checked={cargoStore.additionalServices[item.name]}
            isLiftAvailable={item.name === 'lift' ? form.getFieldValue('loaders') : undefined}
          />
        ))}

        {/* Упаковка */}
        {packages.map(pkg => (
          <PackageControl
            key={pkg.id}
            pkg={pkg}
            onClick={() => handleAddPackage(pkg.id)}
            onChange={value => handleUpdatePackageQty(pkg.id, value)}
          />
        ))}
      </Form>

      <Modal
        open={modal}
        onCancel={hide}
        title="Поднять тяжёлые вещи?"
        footer={footer}
      >
        <Form form={form}>
          <S.LabelTitle>Москва, ул, Ленина, д. 13</S.LabelTitle>
          <FormField {...formFields.floorField} />
          <Space direction="vertical">
            <Form.Item name="liftToFloor">
              <Radio.Group defaultValue="lift">
                <Radio value="lift">На лифте</Radio>
                <Radio value="ladder">По лестнице</Radio>
              </Radio.Group>
            </Form.Item>
          </Space>
        </Form>
      </Modal>
    </S.Wrapper>
  );
};
