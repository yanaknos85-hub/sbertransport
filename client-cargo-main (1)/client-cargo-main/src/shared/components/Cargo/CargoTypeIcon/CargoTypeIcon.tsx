import React, { FC } from 'react';

import { CargoTypeNameEnum } from 'stores/CargoType/CargoType.interface';

import { Icon } from './CargoTypeIcon.style';
import { ReactComponent as IconClothes } from './images/iconClothes.svg';
import { ReactComponent as IconConstrMaterials } from './images/iconConstrMaterials.svg';
import { ReactComponent as IconDocs } from './images/iconDocs.svg';
import { ReactComponent as IconFood } from './images/iconFood.svg';
import { ReactComponent as IconFurniture } from './images/iconFurniture.svg';
import { ReactComponent as IconHouseholdGoods } from './images/iconHouseholdGoods.svg';
import { ReactComponent as IconOther } from './images/iconOther.svg';
import { ReactComponent as IconTableware } from './images/iconTableware.svg';
import { ReactComponent as IconTechnic } from './images/iconTechnic.svg';
import { ReactComponent as IconTools } from './images/iconTools.svg';

const CargoTypeIcon: FC<{ name: CargoTypeNameEnum }> = ({ name }) => (
  <Icon>
    {{
      [CargoTypeNameEnum.TECHNIQUE]: <IconTechnic />,
      [CargoTypeNameEnum.DOCUMENT]: <IconDocs />,
      [CargoTypeNameEnum.DOCUMENT_CARS]: <IconDocs />,
      [CargoTypeNameEnum.TABLEWARE]: <IconTableware />,
      [CargoTypeNameEnum.CLOTHES]: <IconClothes />,
      [CargoTypeNameEnum.FURNITURE]: <IconFurniture />,
      [CargoTypeNameEnum.FOOD_PRODUCTS]: <IconFood />,
      [CargoTypeNameEnum.TOOLS]: <IconTools />,
      [CargoTypeNameEnum.MATERIALS]: <IconConstrMaterials />,
      [CargoTypeNameEnum.HOUSEHOLD_GOODS]: <IconHouseholdGoods />,
      [CargoTypeNameEnum.OTHER]: <IconOther />,
    }[name] || <IconOther />}
  </Icon>
);

export default CargoTypeIcon;
