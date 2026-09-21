from kivy.app import App
from kivy.uix.label import Label


class AbsensiHBCApp(App):
    def build(self):
        return Label(text="Absensi HBC - Build berhasil!", font_size=24)


if __name__ == "__main__":
    AbsensiHBCApp().run()
