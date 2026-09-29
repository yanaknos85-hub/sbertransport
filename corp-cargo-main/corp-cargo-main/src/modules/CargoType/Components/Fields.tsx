import { useEffect, useMemo, useState } from 'react';
import { LabeledValue } from 'antd/lib/select';
import { useCargoTypeNames } from 'api/cargo-type-names';
import { useOrganizationProjection } from 'api/organizations/search';
import { ModelFormFieldProps, ModelFormFieldType } from 'shared/models/ModelDetail/ModelFormField';
import { useCargoCategoryNames } from 'api/cargo-category-names';
import { CargoCategory } from 'stores/CargoType/CargoType.interface';
import { AccessControl } from './types/types'; 
import { useRole } from 'utils/useRole';
import { Roles } from 'constants/constants.app';
import { useHook as useOrganization } from 'context/Organization.context';
import { ValidationRules } from 'shared/fieldValidationRules';
import { getLevelOptions, getMostImportantRole } from './utils/utils';

import styles from '../CargoType.module.scss';  

export const useFields: (
  category?: string,
  accessLevel?: AccessControl,
  isAdding?: boolean,
  onAccessLevelChange?: (level: AccessControl) => void
) => ModelFormFieldProps[] = (category, accessLevel, isAdding = false, onAccessLevelChange) => {
  const { byName: cargoTypeNameByName } = useCargoTypeNames().data;
  const [isChangeFields, setChangeFieldsFields] = useState(false);
  const [unit, setUnit] = useState('');

  const userRoles = useRole();
  const mostImportantRole = getMostImportantRole(userRoles);

  const { organizationId } = useOrganization();
  const organizations = useOrganizationProjection({}, { suspense: false }).data;

  const organizationOptions: LabeledValue[]
  = organizations?.map(({ officialName, id }) => ({ label: officialName, value: id })) ?? [];

  const defaultOrganization = organizationOptions.find(org => org.value === organizationId);

  const { data: cargoCategoryNames } = useCargoCategoryNames();
  const { categoryNames } = cargoCategoryNames;

  const categoryChange = (value: string) => {
    const unit = categoryNames.find((category) => category.name === value)?.unit as string;
    setUnit(unit);
  };

  useEffect(() => {
    categoryChange(category as string);
  }, [category]);

  const isOversized = (category: string) => {
    return category === CargoCategory.BULK || category === CargoCategory.LIQUID;
  };

  const optionsCategory = categoryNames
    .map(({name, value}) => ({ value: name, label: value }));

  const notOversized: any = [
    {
      name: 'volume',
      fieldType: ModelFormFieldType.NUMBER,
      required: true,
      description: `Объём единицы товара(${unit})`,
      editable: true,
      min: 0,
      max: 99.999,
      decimalSeparator: ',',
      step: 0.001,
      rules: [ValidationRules.general.validationNumericNonZero],
    },
  ]

  useEffect(() => {
    if (category && isOversized(category)) {
      setChangeFieldsFields(true);
    }
  }, [category]);

  const handleAccessLevelChange = (value: AccessControl) => {
    if (onAccessLevelChange) {
      onAccessLevelChange(value);
    }
  };

  const accessLevelOptions = getLevelOptions(mostImportantRole as Roles, accessLevel);

  return useMemo<ModelFormFieldProps[]>(() => {
      const baseFields: ModelFormFieldProps[] = [
        {
          name: 'name',
          fieldType: ModelFormFieldType.TEXT,
          description: 'Краткое наименование SKU',
          required: true,
          editable: true,
          className: styles.input,
        },
        {
          name: 'type',
          fieldType: ModelFormFieldType.SELECT,
          description: 'Вид груза',
          // @ts-ignore
          options: Object.values(cargoTypeNameByName).map(({ name, value }) => ({ label: value, value: name })),
          required: true,
          editable: true,
        },
        {
          name: 'category',
          fieldType: ModelFormFieldType.SELECT,
          description: 'Категория',
          options: optionsCategory,
          required: true,
          editable: true,
          onSelect: (value: string) => {
            categoryChange(value);
            if (isOversized(value)) {
              setChangeFieldsFields(true);
            } else {
              setChangeFieldsFields(false);
            }
          },
        },
        {
          name: 'width',
          fieldType: ModelFormFieldType.NUMBER,
          description: 'Ширина единицы товара (мм)',
          required: true,
          editable: true,
          min: 0,
          rules: [ValidationRules.general.validationNumericNonZero],
        },
        {
          name: 'length',
          fieldType: ModelFormFieldType.NUMBER,
          description: 'Длина единицы товара (мм)',
          required: true,
          editable: true,
          min: 0,
          rules: [ValidationRules.general.validationNumericNonZero],
        },
        {
          name: 'height',
          fieldType: ModelFormFieldType.NUMBER,
          description: 'Высота единицы товара (мм)',
          required: true,
          editable: true,
          min: 0,
          rules: [ValidationRules.general.validationNumericNonZero],
        },
        {
          name: 'weight',
          fieldType: ModelFormFieldType.NUMBER,
          description: 'Вес БРУТТО единицы товара (кг)',
          required: true,
          editable: true,
          min: 0,
          rules: [ValidationRules.general.validationNumericNonZero],
        },
        {
          name: 'accessLevel',
          fieldType: ModelFormFieldType.SELECT,
          options: accessLevelOptions,
          description: 'Уровень доступа',
          required: true,
          editable: true,
          rules: [ValidationRules.general.validationNumericNonZero],
          onSelect: handleAccessLevelChange,
          // disabled: getLevelOptions(mostImportantRole as Roles, accessLevel).length <= 1, пока нужно так
        },
      ];
      // Добавляем поле организации только если выбран уровень ORGANIZATION
      const shouldShowOrganizationField = accessLevel === 'ORGANIZATION';

    if (shouldShowOrganizationField) {
      baseFields.push({
        name: 'organizationId',
        fieldType: ModelFormFieldType.SELECT,
        description: 'Организация',
        editable: true,
        options: organizationOptions,
        showSearch: true,
        required: true,
        initialValue: defaultOrganization?.value,
      });
    }
    let fields = [...baseFields];
      
      isChangeFields && fields.map(field => ({
        ...field,
        value: 0,
      }));
      isChangeFields && fields.splice(3, 4);
      isChangeFields && fields.splice(3, 0, ...notOversized)
      return fields;
    },
    [cargoTypeNameByName, isChangeFields, unit,  accessLevel, isAdding, organizationOptions, defaultOrganization]
  );
};
