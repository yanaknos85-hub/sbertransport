import { TFraudMonitoringDetailsResponse } from 'modules/FraudMonitoring/fraudMonitoring.interface';
import { UUID } from 'utils/io-ts';
import { useInformationGroups } from '../components/InformationSection/hooks/useInformationGroups';

export const fraudItemsMock: TFraudMonitoringDetailsResponse['fraudMarkers'] = [
  {
    comment: 'comment1',
    id: '1' as UUID,
  },
  {
    comment: 'comment2',
    id: '2' as UUID,
  },
];

export const fraudDetailsResponseMock: TFraudMonitoringDetailsResponse = {
  id: '1f2a3b4c-d5e6-7f89-g0h1-j2k3l4m5n6op',
  humanReadableId: 'OT-0001-00014844',
  costCenter: '0001L00001',
  transportType: 'YANDEX',
  fraudMarkers: [
    {
      id: '3fa85f64-5717-4562-b3fc-2c963f66afa6' as UUID,
      comment: 'Поездка в ночное время',
    },
    {
      id: '3fa85f64-5717-4562-b3fc-2c963f66afa7' as UUID,
      comment: 'Поездка выходной день',
    },
  ],
  intermediateAddresses: [
    {
      address: 'Красная площадь, Москва',
      waitTime: 15,
    },
    {
      address: 'Невский проспект, Санкт-Петербург',
      waitTime: 30,
    },
  ],
  desiredDate: '2025-11-07T11:14:44.703Z',
  passenger: {
    id: '1f2a3b4c-d5e6-7f89-g0h1-j2k3l4m5n6oq' as UUID,
    lastName: 'Иванов',
    firstName: 'Иван',
    patronymic: 'Иванович',
    personnelNumber: 'EMP-001',
  },
  approvalDate: '2025-11-07T11:14:44.703Z',
  approver: {
    id: '1f2a3b4c-d5e6-7f89-g0h1-j2k3l4m5n6or' as UUID,
    lastName: 'Петров',
    firstName: 'Петр',
    patronymic: 'Петрович',
    personnelNumber: 'MAN-001',
  },
  autoApproval: true,
  waypointsCount: 2,
  departureAddress: 'Москва, ул. Тверская, д. 1',
  destinationAddress: 'Санкт-Петербург, Невский проспект, д. 35',
  plannedCost: 5000,
  actualCost: 4800,
  distance: 700,
  status: 'COMPLETED',
  compensationType: 'PARTIAL',
  purpose: {
    id: '1f2a3b4c-d5e6-7f89-g0h1-j2k3l4m5n6os' as UUID,
    purpose: 'Бизнес-командировка',
  },
  department: 'IT-отдел',
  departmentCode: 'ITD-001',
  tripEndDate: '2025-11-07T11:14:44.703Z',
};

export const groupsMock: ReturnType<typeof useInformationGroups> = [
  {
    groupName: 'groupName1',
    fields: [{
      title: 'groupName1Title',
      value: 'groupName1Value',
    }],
  },
  {
    groupName: 'groupName2',
    fields: [{
      title: 'groupName2Title',
      value: 'groupName2Value',
    }],
  },
];

export const groupsWithNoFieldsMock: ReturnType<typeof useInformationGroups> = [
  {
    groupName: 'groupName1',
    fields: [],
  },
];

export const groupsWithNullValueMock: ReturnType<typeof useInformationGroups> = [
  {
    groupName: 'groupName1',
    fields: [{
      title: 'title',
      value: null,
    }],
  },
];
