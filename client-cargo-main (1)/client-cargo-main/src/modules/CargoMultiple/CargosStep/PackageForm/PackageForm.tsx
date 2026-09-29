import React, { FC, useEffect, useState } from 'react';
import { PlusOutlined } from '@ant-design/icons';
import { Checkbox, Form } from 'antd';
import Button from 'antd/es/button/button';
import { CheckboxChangeEvent } from 'antd/lib/checkbox';
import { observer } from 'mobx-react';
import { ValidationRules } from 'shared/fieldValidationRules';
import { FieldType } from 'shared/form/Field/Field';
import FormField from 'shared/form/FormField/FormField';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { Pack } from 'stores/Cargos/types';
import { StoreNames } from 'stores/StoreNames.enum';

import { ReactComponent as TrashBucket } from '../../AddressStep/AddressForm/images/trash.svg';
import { PackageContainer, PackageTitle } from '../Cargos.style';
import * as S from './PackageForm.style';

const PackageForm: FC = observer(() => {
  const {
    [StoreNames.cargoStore]: cargoStore,
  } = useAppStoreContext();

  const [form] = Form.useForm();
  const [packageOptions, setPackageOptions] = useState<{ name: string; value: string }[]>([]);

  useEffect(() => {
    const getPacks = async () => {
      await cargoStore.getPack();
    };

    getPacks()
      .then(() => {
        const options = cargoStore.packs?.reduce((acc: { name: string; value: string }[], cur: Pack) => {
          const option = { name: cur.id, value: cur.name };
          return [...acc, option];
        }, []);
        setPackageOptions(options);
      })
      .catch();
  }, []);

  const field = {
    loaders: {
      label: '',
      name: 'loaders',
      type: FieldType.number,
      rules: [ValidationRules.general.required],
    },
    loadersCount: {
      label: 'Количество',
      name: 'loadersCount',
      type: FieldType.number,
      rules: [ValidationRules.general.required, ValidationRules.general.min(1)],
      initialValue: 1,
    },
    package: {
      label: 'Упаковка',
      type: FieldType.select,
      rules: [ValidationRules.general.required],
      params: {
        options: packageOptions,
        style: { width: '50%' },
        onChange: () => {
          cargoStore.savePackageForm({ ...cargoStore.packageForm, packages: form.getFieldValue('packages') });
        },
      },
    },
    packageCount: {
      label: 'Количество',
      type: FieldType.number,
      rules: [ValidationRules.general.required, ValidationRules.general.min(1)],
      initialValue: 1,
      params: {
        min: 1,
        onChange: () => {
          cargoStore.savePackageForm({ ...cargoStore.packageForm, packages: form.getFieldValue('packages') });
        },
      },
    },
  };

  const handleCheckbox = (event: CheckboxChangeEvent) => {
    cargoStore.setCheckLoaders(event.target.checked);
    cargoStore.savePackageForm({
      ...cargoStore.packageForm,
      loadersNeeded: event.target.checked,
      loadersCount: event.target.checked ? 1 : 0,
    });
  };

  const handleDelete = () => {
    cargoStore.savePackageForm({
      ...cargoStore.packageForm,
      packages: form.getFieldValue('packages'),
    });
  };

  return (
    <PackageContainer>
      <PackageTitle>
        Упаковка и грузчики
      </PackageTitle>
      <Form
        form={form}
        name="package"
        initialValues={{
          packages: cargoStore.packageForm?.packages,
        }}
      >
        <S.CheckboxWrapper>
          <Checkbox
            checked={cargoStore.packageForm.loadersNeeded}
            name="Требуются грузчики"
            onChange={(event: CheckboxChangeEvent) => handleCheckbox(event)}
          >
            Требуются грузчики
          </Checkbox>
        </S.CheckboxWrapper>
        <Form.List name="packages">
          {(packages, { add, remove }) => {
            return (
              <>
                {packages.map(({ key, name }) => (
                  <S.PackageItem key={key}>
                    <S.PackageName>
                      <FormField {...field.package} name={[name, 'package']} />
                    </S.PackageName>
                    <S.PackageCount>
                      <FormField {...field.packageCount} name={[name, 'packageCount']} />
                      <S.TrashIcon>
                        <TrashBucket
                          onClick={() => {
                            remove(name);
                            handleDelete();
                          }}
                        />
                      </S.TrashIcon>
                    </S.PackageCount>
                  </S.PackageItem>
                ))}
                <Form.Item>
                  <Button
                    disabled
                    onClick={() => add()}
                    icon={<PlusOutlined />}
                  >
                    Добавить упаковку
                  </Button>
                </Form.Item>
              </>
            );
          }}
        </Form.List>
      </Form>
    </PackageContainer>
  );
});

export default PackageForm;
