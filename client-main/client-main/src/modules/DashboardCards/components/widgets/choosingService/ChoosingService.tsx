import React, { Dispatch } from 'react';
import { WrapperChoosingService } from './styledChoosingService';
import { Select } from 'antd';
import { ServiceWidgetsEnum, ServiceWidgetsTitle } from 'modules/DashboardCards/constants/widgets.constants';

interface ChoosingServiceProps {
  selected: string;
  chandgeSelected: Dispatch<React.SetStateAction<ServiceWidgetsEnum>>;
}

const optionsChoosingService = [
  { value: ServiceWidgetsEnum.ALL, label: ServiceWidgetsTitle[ServiceWidgetsEnum.ALL] },
  { value: ServiceWidgetsEnum.PASSENGER, label: ServiceWidgetsTitle[ServiceWidgetsEnum.PASSENGER] },
];

const ChoosingService: React.FC<ChoosingServiceProps> = ({ selected, chandgeSelected }) => {
  const handleChange = (value: ServiceWidgetsEnum) => {
    chandgeSelected(value);
  };

  return (
    <WrapperChoosingService>
      <Select
        defaultValue={selected}
        style={{ width: 120 }}
        onChange={handleChange}
        options={optionsChoosingService}
      />
    </WrapperChoosingService>
  );
};

export default ChoosingService;
