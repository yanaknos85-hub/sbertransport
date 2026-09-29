/**
 * Mutate dom element ant-message relatively open or closed menu. Unfortinatuly this element has absolute coords.
 */
export function useMenuResizingSpy(): void {
  const domMessage = document.querySelector('.ant-message-notice-content') as HTMLElement;
  const domSideMenu = document.querySelector('.side-menu-container') as HTMLElement;

  const sideMenuWidth = domSideMenu && getComputedStyle(domSideMenu).width;

  if (domMessage && domSideMenu && sideMenuWidth) {
    domMessage.style.marginLeft = sideMenuWidth;
  }
}
