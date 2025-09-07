const { app, BrowserWindow } = require('electron');
const path = require('path');

function createWindow() {
    const mainWindow = new BrowserWindow({
        width: 400, // Ajusta el ancho de la ventana
        height: 600, // Ajusta la altura de la ventana
        resizable: false, // Opcional: evita que el usuario cambie el tamaño de la ventana
        autoHideMenuBar: true, // Opcional: oculta el menú de la aplicación
        webPreferences: {
            nodeIntegration: true,
            contextIsolation: false,
        }
    });

    // Carga tu archivo HTML
    mainWindow.loadFile('calc.html');
}

app.whenReady().then(() => {
    createWindow();

    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) {
            createWindow();
        }
    });
});

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
        app.quit();
    }
});