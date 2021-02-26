(ns rummikub-ctmx.service.state
  (:require
    [clojure.set :as set]
    [rummikub-ctmx.util :as util]))

(def colors [:red :yellow :blue :black])
(def numbers (range 1 14))
(defn enumerate [s]
  (map-indexed list s))

(def tiles
  (concat
    (for [color colors number numbers i (range 2)]
      [color number i])
    [[:red 0 0] [:black 0 0]]))

(defonce state
  (atom
    {:pool (set tiles)
     :players {} ;; tile -> [player i j]
     :table {} ;; tile -> [x y]
     }))

(defn tiles-for-player
  ([player]
   (->> @state :players (tiles-for-player player)))
  ([player players]
   (for [[tile [name]] players :when (= name player)]
     tile)))

(defn sort-tiles [player tiles]
  (let [{:keys [red yellow blue black]} (group-by first tiles)
        red (sort-by second red)
        yellow (sort-by second yellow)
        blue (sort-by second blue)
        black (sort-by second black)
        first-row (concat red yellow)
        second-row (concat blue black)]
    (into {}
          (for [[i row] (enumerate [first-row second-row])
                [j tile] (enumerate row)]
            [tile [player i j]]))))

(defn sort-player [state player]
  (->> state
       :players
       (tiles-for-player player)
       (sort-tiles player)
       (update state :players merge)))
(defn sort-player! [player]
  (swap! state sort-player player))

(def to-take 14)
(defn pick-up [{:keys [pool players current] :as s} player]
  (assert (every? #(-> % second first (not= player)) players))
  (let [to-pick (->> pool shuffle (take to-take))
        sorted (sort-tiles player to-pick)]
    (assoc s
      :pool (set/difference pool (set to-pick))
      :players (merge players sorted)
      :current (or current player))))
(defn pick-up-one [{:keys [pool players] :as s} player]
  (if (empty? pool)
    s
    (let [to-pick (-> pool seq rand-nth)
          first-row (for [[tile [p i]] players :when (= [p i] [player 0])] tile)]
      (assoc s
        :pool (disj pool to-pick)
        :players (assoc players to-pick [player 0 (-> first-row count inc)])))))

(defn pick-up-new! [player]
  (swap! state pick-up player))
(defn pick-up-one! [player]
  (swap! state pick-up-one player))

(defn put-down [{:keys [players table] :as s} tile x y]
  (assoc s
    :players (dissoc players tile)
    :table (assoc table tile [x y])))
(defn put-down! [tile [x y]]
  (swap! state put-down tile x y))

(defn pick-up-used [{:keys [players table] :as s} row player i]
  (assoc s
    :players (into players
                   (for [[j tile] (enumerate row)]
                     [tile [player i j]]))
    :table (apply dissoc table row)))
(defn pick-up-used! [row player i]
  (swap! state pick-up-used row player i))

(defn- next-player [players current]
  (let [all (->> players vals (map first) set)]
    (case (count all)
      0 nil
      1 (first all)
      (->> (conj all current)
           sort
           cycle
           (drop-while #(not= % current))
           second))))

(defn quit [{:keys [pool players current] :as s} player]
  (let [to-remove (tiles-for-player player players)
        players (apply dissoc players to-remove)]
    (assoc s
      :pool (set/union pool (set to-remove))
      :players players
      :current (next-player players current))))
(defn quit! [player]
  (swap! state quit player))

(defn next-turn [s]
  (assoc s :current (next-player (:players s) (:current s))))
(defn next-turn! []
  (swap! state next-turn))

(defn users []
  (->> @state :players vals (map first) set))

(defn third [x]
  (get x 2))
(defn player-state [player]
  (let [{:keys [players table current]} @state]
    {:table table
     :current current
     :rows
     (->> players
          (filter #(-> % second first (= player)))
          (sort-by #(-> % second third))
          (reduce (fn [m [tile [_ i]]]
                    (update m i conj tile)) [[] []]))}))

(defn swap-players [s player1 player2]
  (let [player-swap #({player1 player2 player2 player1} % %)]
    (update s :players
            (fn [players]
              (util/map-vals #(update % 0 player-swap) players)))))
(defn swap-players! [player1 player2]
  (swap! state swap-players player1 player2))

(defn reset-game [s]
  (let [player-names (->> s :players vals (map first) distinct)]
    (reduce
      pick-up
      {:pool (set tiles)
       :players {}
       :table {}}
      player-names)))
(defn reset-game! []
  (swap! state reset-game))

(defn save-state []
  (spit "save.edn" (pr-str @state)))
(defn load-state []
  (->> "save.edn" slurp read-string (reset! state)))
