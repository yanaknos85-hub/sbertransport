import { usePublicCompensations } from 'api/compensations';

interface SelectOption {
  value: string;
  label: string;
}

export const useSelectablePublicCompensations = () => {
  const compensationOptions: SelectOption[] = usePublicCompensations().data.publicCompensations.map(el => ({
    label: el.rusName,
    value: el.name,
  }));

  return { compensationOptions };
};
