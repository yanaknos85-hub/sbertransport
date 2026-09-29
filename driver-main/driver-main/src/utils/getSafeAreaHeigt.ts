export const getSafeAreaHeight = () => {
  const safeAreaElem = document.querySelector('.st-safe-area') as HTMLElement | null;

  return safeAreaElem?.offsetHeight ?? 0;
};
