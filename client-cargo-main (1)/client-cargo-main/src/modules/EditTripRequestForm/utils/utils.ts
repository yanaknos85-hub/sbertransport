export const checkPurposeValidity = (rule: boolean, isPurpose?: boolean): Promise<void> => rule
  ? Promise.resolve()
  : Promise.reject(
    new Error(
      isPurpose
        ? 'Укажите цель поездки, не противоречащую условиям запланированной поездки'
        : 'Укажите дату, не противоречащую условиям выбранной цели поездки'
    )
  );
