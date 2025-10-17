chrome.runtime.onInstalled.addListener(() => {
  if (chrome.sidePanel) {
    chrome.sidePanel.setPanelBehavior({ openPanelOnActionClick: true }).catch((error) => {
      console.error('Side panel behavior error', error);
    });
  }
});

chrome.tabs.onUpdated.addListener(async (tabId, changeInfo, tab) => {
  if (!chrome.sidePanel || changeInfo.status !== 'complete' || !tab.url) {
    return;
  }
  const url = new URL(tab.url);
  const isLinkedIn = url.hostname === 'www.linkedin.com';

  try {
    if (isLinkedIn) {
      await chrome.sidePanel.setOptions({
        tabId,
        path: 'sidepanel/index.html',
        enabled: true
      });
    } else {
      await chrome.sidePanel.setOptions({
        tabId,
        enabled: false
      });
    }
  } catch (error) {
    console.error('Unable to set side panel options', error);
  }
});
