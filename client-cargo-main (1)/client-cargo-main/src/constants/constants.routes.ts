export const EXTERNAL = '/client'; // разводная точка маршрутизации между MF

export const APP = '/cargo'; // главная точка маршрутизации приложения

export const MAIN = `${EXTERNAL}${APP}`;

export const AUTH = '/oauth';

export const CARGO = `${MAIN}/single`;
export const CARGO_CREATE = `${CARGO}/create`;
export const EXCHANGE = `${CARGO}/exchange`;

export const REPEAT_ORDER = `${CARGO_CREATE}?step=3`; // Повтор заявки

export const MULTIPLE_CARGO = `${MAIN}/multiple`;
export const MULTIPLE_CARGO_CREATE = `${MULTIPLE_CARGO}/create`;

export const CARGOS = `${CARGO}/list`;
export const CARGOS_JOURNAL = `${CARGOS}/:filter`;
export const CARGOS_DETAILED = `${CARGOS}/:filter/:reqId`;

export const REGULAR_CARGO = `${MAIN}/regular`;
export const REGULAR_CARGO_CREATE = `${REGULAR_CARGO}/create`;

export const REGULAR_CARGOS = `${REGULAR_CARGO}/list`;
export const REGULAR_CARGOS_JOURNAL = `${REGULAR_CARGOS}/:filter`;
export const REGULAR_CARGOS_DETAILED = `${REGULAR_CARGOS}/:filter/:reqId`;

export const MULTIPLE_REGULAR_CARGO = `${MAIN}/multiregular`;
export const MULTIPLE_REGULAR_CARGO_CREATE = `${MULTIPLE_REGULAR_CARGO}/create`;

export const MULTIPLE_REGULAR_CARGOS = `${MULTIPLE_REGULAR_CARGO}/list`;
export const MULTIPLE_REGULAR_CARGOS_JOURNAL = `${MULTIPLE_REGULAR_CARGOS}/:filter`;
export const MULTIPLE_REGULAR_CARGOS_DETAILED = `${MULTIPLE_REGULAR_CARGOS}/:filter/:reqId`;

export const MASS_CARGO = `${MAIN}/mass`;
export const MASS_CARGO_CREATE = `${MASS_CARGO}/single/create`;
export const MASS_REGULAR_CARGO_CREATE = `${MASS_CARGO}/regular/create`;
export const MASS_CARGO_DEMO = `${MASS_CARGO}/demo`;

export const MASS_CARGO_MULTIPLE = `${MAIN}/mass-multiple`;
export const MASS_CARGO_CREATE_MULTIPLE = `${MASS_CARGO_MULTIPLE}/single/create`;
export const MASS_REGULAR_CARGO_CREATE_MULTIPLE = `${MASS_CARGO}/regular/create`;

export const APPROVEMENT = `${MAIN}/approvement`;
export const APPROVEMENT_CARGO = `${APPROVEMENT}/cargo`;
export const APPROVEMENT_CARGOS = `${APPROVEMENT_CARGO}/single`;
export const APPROVEMENT_CARGOS_JOURNAL = `${APPROVEMENT_CARGOS}/:filter`;
export const APPROVEMENT_CARGOS_DETAILED = `${APPROVEMENT_CARGOS}/:filter/:reqId`;

export const APPROVEMENT_COMPENSATION = `${APPROVEMENT}/compensation`;
export const APPROVEMENT_COMPENSATION_JOURNAL = `${APPROVEMENT_COMPENSATION}/:filter`;

export const APPROVEMENT_REGULAR_CARGOS = `${APPROVEMENT_CARGO}/regular`;
export const APPROVEMENT_REGULAR_CARGOS_JOURNAL = `${APPROVEMENT_REGULAR_CARGOS}/:filter`;
export const APPROVEMENT_REGULAR_CARGOS_DETAILED = `${APPROVEMENT_REGULAR_CARGOS}/:filter/:reqId`;

