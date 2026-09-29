import { renderHook } from '@testing-library/react-hooks';
import { useModalForm } from '../useModalForm';
import { Contract } from 'stores/Contracts/Contracts.interface';
import { useModal } from '../../../context/modal.context';
import { useProfile } from 'api/profile';
import { useCreateContract, useUpdateContract } from 'api/contracts';
import { useContractors } from 'api/contractors';
import { serviceTypesDefaultValuePassengers } from '../../../constants/constants';
import moment from 'moment';
import { DATE_FORMAT } from 'constants/constants.app';
import { UUID } from 'utils/io-ts';

// Моки зависимостей
jest.mock('../../../context/modal.context', () => ({
  useModal: jest.fn(),
}));

jest.mock('api/profile', () => ({
  useProfile: jest.fn(),
}));

jest.mock('api/contracts', () => ({
  useCreateContract: jest.fn(),
  useUpdateContract: jest.fn(),
}));

jest.mock('api/contractors', () => ({
  useContractors: jest.fn(),
}));

const mockContract: Contract = {
  id: '1' as UUID,
  startDate: '2023-01-01',
  endDate: '2023-12-31',
  organizationIds: ['org1' as UUID],
  includeVat: true,
  vatValue: 20,
  driverLatePickupPenalty: 0.1,
  poorServiceQualityPenalty: 0.1,
  driverOrderCancellationPenalty: 0.1,
  contractorId: 'contractorId' as UUID,
  transportType: 'type' as UUID,
  applyTo: undefined,
  sum: 1000,
  active: true,
  contractNumber: 'contractNumber',
  regionIds: ['region'],
};

const mockModalState = {
  type: 'edit',
};

const mockProfile = {
  data: {
    organizationId: 'org1',
  },
};

const mockContractors = {
  data: {
    contractors: [],
  },
};

const mockCreateContract = jest.fn().mockResolvedValue({});
const mockEditContract = jest.fn().mockResolvedValue({});
const mockCloseModal = jest.fn();

describe('useModalForm', () => {
  beforeEach(() => {
    (useModal as jest.Mock).mockReturnValue({
      modalState: mockModalState,
      closeModal: mockCloseModal,
    });
    (useProfile as jest.Mock).mockReturnValue(mockProfile);
    (useContractors as jest.Mock).mockReturnValue(mockContractors);
    (useCreateContract as jest.Mock).mockReturnValue([mockCreateContract]);
    (useUpdateContract as jest.Mock).mockReturnValue([mockEditContract]);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('должен инициализировать форму с правильными начальными значениями', () => {
    const { result } = renderHook(() => useModalForm(mockContract));

    expect(result.current.initialValues).toEqual({
      ...mockContract,
      organizationIds: mockContract.organizationIds,
      serviceType: serviceTypesDefaultValuePassengers.value,
      period: [moment(mockContract.startDate, DATE_FORMAT.BASE), moment(mockContract.endDate, DATE_FORMAT.BASE)],
      driverLatePickupPenalty: mockContract.driverLatePickupPenalty,
      poorServiceQualityPenalty: mockContract.poorServiceQualityPenalty,
      driverOrderCancellationPenalty: mockContract.driverOrderCancellationPenalty,
      vatValue: mockContract.vatValue !== undefined ? String(mockContract.vatValue) : undefined,
    });
  });
});
