import React, { FC } from 'react';

import { Menu } from 'antd';
import { CheckOutlined, ExclamationCircleOutlined, MinusCircleOutlined } from '@ant-design/icons';

import { DownloadFileList, useDownloadRegistry } from 'api/reports';

import { InfoContractor, Month } from 'modules/TaxiRegistry/types/types';
import { capitalize } from 'modules/TaxiRegistry/utils';
import moment from 'moment';

interface OwnProps {
  setVisible(visible: boolean): void;
  setMenuVisible(visible: boolean): void;
  month: Month;
  monthInd: number;
  setModalData({
    // @ts-ignore
    contractorId, monthName: monthId, contractorName,
  }: InfoContractor): void;
  contractorId: string;
  contractorName: string;
  monthListContractor: DownloadFileList[];
}
export const ShowMenuList: FC<OwnProps> = props => {
  const {
    setVisible,
    setMenuVisible,
    month,
    monthInd,
    setModalData,
    contractorId,
    contractorName,
    monthListContractor,
    ...rest
  } = props;

  const getIsValid = (arr: DownloadFileList[] | []): boolean | undefined => {
    const contractor = arr.find(l => l.contractor.id === contractorId);
    if (contractor) {
      const monthIn = contractor.taxiTripRegisters.findIndex(l => Number(l.date.split('-')[1]) === monthInd + 1);
      const valid = contractor.taxiTripRegisters[monthIn]?.valid;
      return valid;
    }
    return undefined;
  };
  const [downloadFile] = useDownloadRegistry(contractorId, contractorName);
  const isValid = getIsValid(monthListContractor);
  const colorButton = (valid: boolean | undefined) => typeof valid === 'undefined' ? 'red' : valid ? 'green' : 'orange';

  const iconSubMenu = (valid: boolean | undefined) => typeof valid === 'undefined' ? <MinusCircleOutlined /> : valid ? <CheckOutlined /> : <ExclamationCircleOutlined />;

  const handleClick = (
    valid: boolean | undefined,
    contractorId: string,
    monthName: string,
    contractorName: string,
    selectedDate: Month
  ) => typeof valid === 'undefined'
    ? (setVisible(true), setMenuVisible(false), setModalData({
      contractorId, monthName, contractorName,
    }))
    : downloadFile({ year: selectedDate.year.toString(), month: moment().month(monthName).format('MM').toString() });

  return (
    <Menu.Item
      {...rest}
      style={{ color: colorButton(isValid) }}
      onClick={() => handleClick(isValid, contractorId, month.title, contractorName, month)}
      icon={iconSubMenu(isValid)}
    >
      {capitalize(`${month.title} ${month.year}`)}
    </Menu.Item>
  );
};
