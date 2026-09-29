export const usePlatformDetect = () => {
  const { userAgent } = navigator;

  const isAndroid = Boolean(userAgent.match(/Android/i));
  const isIos = Boolean(userAgent.match(/iPhone|iPad|iPod/i));

  const isMobile = isAndroid || isIos;
  const isDesktop = !isMobile;

  return {
    isAndroid,
    isIos,
    isMobile,
    isDesktop,
  };
};
