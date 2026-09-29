import { IS_DEV } from 'constants/constants.env';

export const MOCK_ID = '3fa85f64-5717-4562-b3fc-2c963f66afa6';

export const FILL_FORM = IS_DEV && /fill/.test(window.location.search);

export const INITIAL_STEP = IS_DEV ? Number((window.location.search.match(/step=(\d)/) || [1, 1])[1]) : 1;

export const STEPS = {
  address: 1,
  cargos: 2,
  tariff: 3,
  final: 4,
};

export const SWAP_STEPS = Object.fromEntries(Object.entries(STEPS).map(a => a.reverse()));

export const MAX_STEP = 4;

export const DEFAULT_FORM_ERROR = 'Необходимо заполнить форму';
export const RELOCATION_ERROR = 'Необходимо добавить хотя бы один одну вещь';

const { origin } = window.location;

export const IS_TEST_STAND
  = origin.includes('localhost')
  || origin.includes('.cargo.')
  || origin.includes('.ift.')
  || origin.includes('psi.')
  || origin.includes('.psi-gen.')
  || origin.includes('.psi-gen2.')
  || origin.includes('.nt.')
  || origin.includes('.st.');
