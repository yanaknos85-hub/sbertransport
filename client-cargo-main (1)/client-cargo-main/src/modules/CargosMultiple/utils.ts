type AsyncAction = () => Promise<boolean>;

export const makeDelay = (route: string, place = 'active', timeout = 300) => {
  setTimeout(() => {
    window.location.pathname = route.replace(':filter', place);
  }, timeout);
};

export const performAndRedirect = async (
  action: AsyncAction,
  route: string
): Promise<void> => {
  if (await action()) {
    makeDelay(route);
  }
};

export const searchSymbol = (input: string, value?: any): boolean => (
  value?.label.toLowerCase().indexOf(input.toLowerCase()) >= 0
);
