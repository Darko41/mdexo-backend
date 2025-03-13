import SearchBar from "./SearchBar";

export default function Main() {
  return (
    <section className="flex h-72 w-full flex-col items-center justify-center bg-[url('/bgImage.jpg')] bg-contain bg-center bg-no-repeat">
      <div className="flex w-100 flex-col items-start">
        <p className="bg mb-3 pt-12 text-4xl font-bold text-wrap text-black">
          ПРОНАЂИТЕ ВАШ ДОМ ИЗ СНОВА
        </p>
        <SearchBar />
      </div>
    </section>
  );
}
